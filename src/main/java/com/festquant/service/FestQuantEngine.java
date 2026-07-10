package com.festquant.service;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.AuctionBidView;
import com.festquant.domain.AuctionWinner;
import com.festquant.domain.Bid;
import com.festquant.domain.Event;
import com.festquant.domain.PriceRecommendation;
import com.festquant.domain.User;
import com.festquant.model.ForecastResult;
import com.festquant.model.JavaNonlinearModelAdapter;
import com.festquant.model.TimeSeriesForecaster;
import com.festquant.pricing.PriceSimulationRow;
import com.festquant.pricing.RevenueMaximizingPricingStrategy;
import com.festquant.repository.CsvBidRepository;
import com.festquant.repository.CsvEventRepository;
import com.festquant.repository.CsvPricingEventRepository;
import com.festquant.repository.CsvSalesRepository;
import com.festquant.repository.CsvUserRepository;
import com.festquant.repository.JdbcAuctionRepository;
import com.festquant.repository.JdbcEventRepository;
import com.festquant.repository.JdbcPriceRepository;
import com.festquant.util.JsonExporter;
import com.festquant.util.PricingExporter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Application service integrating the forecasting/pricing and auction modules.
 * It also acts as the single transaction boundary for CSV, JSON and JDBC I/O.
 */
@Service
public final class FestQuantEngine implements AnalyticsFacade {
    private static final String JDBC_URL = "jdbc:h2:file:./data/festquant";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final CsvEventRepository eventRepository;
    private final ForecastService forecastService;
    private final DynamicPricingService pricingService;
    private final AuctionService auctionService;
    private final CsvBidRepository bidRepository;
    private final CsvUserRepository userRepository;
    private final PricingExporter pricingExporter = new PricingExporter();
    private final JdbcEventRepository jdbcEventRepository = new JdbcEventRepository(JDBC_URL);
    private final JdbcPriceRepository jdbcPriceRepository = new JdbcPriceRepository(JDBC_URL);
    private final JdbcAuctionRepository jdbcAuctionRepository = new JdbcAuctionRepository(JDBC_URL);
    private final Map<String, AuctionResult> auctionCache = new ConcurrentHashMap<>();
    private volatile List<ForecastResult> forecastCache = List.of();
    private volatile DynamicPricingService.PricingRunResult pricingCache;

    public FestQuantEngine() {
        Path events = root.resolve("data/input/events.csv");
        Path sales = root.resolve("data/input/ticket_sales_timeseries.csv");
        eventRepository = new CsvEventRepository(events);

        CsvPricingEventRepository pricingEvents = new CsvPricingEventRepository(events);
        CsvSalesRepository salesRepository = new CsvSalesRepository(sales);
        TimeSeriesForecaster forecaster = new TimeSeriesForecaster();
        JavaNonlinearModelAdapter nonlinearModel = new JavaNonlinearModelAdapter();
        nonlinearModel.loadFromPythonModel(root.resolve("models/nonlinear_demand_model.json"));

        forecastService = new ForecastService(pricingEvents, salesRepository, forecaster);
        pricingService = new DynamicPricingService(
                pricingEvents,
                salesRepository,
                forecaster,
                new RevenueMaximizingPricingStrategy(nonlinearModel)
        );
        bidRepository = new CsvBidRepository(
                root.resolve("data/input/premium_bids.csv"), new DataValidationService()
        );
        userRepository = new CsvUserRepository(root.resolve("data/input/users.csv"));
        auctionService = new AuctionService(
                eventRepository,
                bidRepository,
                new ExportService(new JsonExporter())
        );
    }

    @PostConstruct
    public void initialise() {
        try {
            jdbcEventRepository.createTable();
            jdbcPriceRepository.createTable();
            jdbcAuctionRepository.createTable();
            for (Event event : events()) {
                jdbcEventRepository.save(event);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialise the embedded H2 database.", exception);
        }
        forecastCache = forecastService.forecastAll();
        refreshRecommendations();
    }

    @Override
    public List<Event> events() {
        return eventRepository.findAllEvents();
    }

    @Override
    public List<ForecastResult> forecasts() {
        if (forecastCache.isEmpty()) {
            forecastCache = forecastService.forecastAll();
        }
        return forecastCache;
    }

    @Override
    public List<PriceRecommendation> recommendations() {
        if (pricingCache == null) {
            return refreshRecommendations();
        }
        return pricingCache.getRecommendations();
    }

    @Override
    public synchronized List<PriceRecommendation> refreshRecommendations() {
        forecastCache = forecastService.forecastAll();
        pricingCache = pricingService.recommendAllPrices();
        Path output = root.resolve("data/output");
        pricingExporter.exportRecommendationsJson(pricingCache.getRecommendations(), output.resolve("price_recommendations.json"));
        pricingExporter.exportRecommendationsCsv(pricingCache.getRecommendations(), output.resolve("price_recommendations.csv"));
        pricingExporter.exportSimulationCsv(pricingCache.getSimulations(), output.resolve("pricing_simulation_details.csv"));
        pricingExporter.exportReport(pricingCache.getRecommendations(), output.resolve("dynamic_pricing_report.txt"));
        try {
            for (PriceRecommendation recommendation : pricingCache.getRecommendations()) {
                jdbcPriceRepository.save(recommendation);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not persist price recommendations.", exception);
        }
        return pricingCache.getRecommendations();
    }

    @Override
    public List<PriceSimulationRow> simulations(String eventId) {
        if (pricingCache == null) {
            refreshRecommendations();
        }
        return pricingCache.getSimulations().stream()
                .filter(row -> row.getEventId().equals(eventId))
                .toList();
    }

    @Override
    public List<AuctionBidView> auctionBids(String eventId) {
        Map<String, String> namesById = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getUserId, User::getName));
        return bidRepository.findBidsByEventId(eventId).stream()
                .sorted(Comparator.comparingDouble(Bid::getBidAmount).reversed()
                        .thenComparing(Bid::getBidTime))
                .map(bid -> new AuctionBidView(
                        namesById.getOrDefault(bid.getUserId(), "Registered Participant"),
                        bid.getBidAmount(),
                        bid.getBidTime()
                ))
                .toList();
    }

    @Override
    public AuctionResult runAuction(String eventId) {
        double recommendedPrice = recommendations().stream()
                .filter(item -> item.getEventId().equals(eventId))
                .mapToDouble(PriceRecommendation::getRecommendedPrice)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No price recommendation for " + eventId));
        AuctionResult result = auctionService.runAuction(eventId, recommendedPrice);
        result = withBidderNames(result);
        auctionService.exportAuctionResult(result, root.resolve("data/output/auction_results.json"));
        try {
            jdbcAuctionRepository.save(result);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not persist auction result.", exception);
        }
        auctionCache.put(eventId, result);
        return result;
    }

    @Override
    public AuctionResult runVickreyAuction(String eventId) {
        double recommendedPrice = recommendations().stream()
                .filter(item -> item.getEventId().equals(eventId))
                .mapToDouble(PriceRecommendation::getRecommendedPrice)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No price recommendation for " + eventId));
        AuctionResult result = auctionService.runVickreyAuction(eventId, recommendedPrice);
        result = withBidderNames(result);
        auctionService.exportAuctionResult(result, root.resolve("data/output/auction_results.json"));
        try {
            jdbcAuctionRepository.save(result);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not persist Vickrey auction result.", exception);
        }
        auctionCache.put(eventId, result);
        return result;
    }

    @Override
    public AuctionResult auctionResult(String eventId) {
        return auctionCache.computeIfAbsent(eventId, this::runAuction);
    }

    /**
     * Auction algorithms deliberately work with stable user IDs. Before sending
     * results to the browser or JSON exporter, the application service enriches
     * winners with display names from the participant repository.
     */
    private AuctionResult withBidderNames(AuctionResult result) {
        Map<String, String> namesById = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getUserId, User::getName));
        List<AuctionWinner> namedWinners = result.getWinners().stream()
                .map(winner -> new AuctionWinner(
                        winner.getUserId(),
                        namesById.getOrDefault(winner.getUserId(), winner.getUserId()),
                        winner.getBidAmount(),
                        winner.getFinalPayment()
                ))
                .toList();
        return new AuctionResult(
                result.getEventId(),
                result.getAuctionType(),
                result.getPremiumSeats(),
                namedWinners,
                result.getClearingPrice(),
                result.getReservePrice(),
                result.getFinalPayment(),
                result.getExplanation()
        );
    }

    @Override
    public Map<String, Object> dashboardSummary() {
        List<Event> events = events();
        List<PriceRecommendation> prices = recommendations();
        double projectedRevenue = prices.stream().mapToDouble(PriceRecommendation::getExpectedRevenue).sum();
        double averageLift = prices.stream()
                .mapToDouble(item -> (item.getRecommendedPrice() / item.getBasePrice() - 1.0) * 100.0)
                .average().orElse(0.0);
        long rising = prices.stream().filter(item -> "RISING".equals(item.getTrendLabel())).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("system", "FestQuant");
        summary.put("status", "LIVE");
        summary.put("eventCount", events.size());
        summary.put("projectedRevenue", Math.round(projectedRevenue * 100.0) / 100.0);
        summary.put("averagePriceLift", Math.round(averageLift * 10.0) / 10.0);
        summary.put("risingEvents", rising);
        summary.put("auctionsRun", auctionCache.size());
        summary.put("pipeline", List.of("Holt forecast", "Nonlinear demand", "Revenue optimisation",
                "Vickrey auction", "H2 persistence"));
        return summary;
    }
}
