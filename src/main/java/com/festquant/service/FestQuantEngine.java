/**
 * Contains the fest quant engine implementation used by FestQuant.
 */
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
    // Stores the jdbc url used by this class.
    private static final String JDBC_URL = "jdbc:h2:file:./data/festquant";
    private final Path root = Path.of("").toAbsolutePath().normalize();
    // Stores the event repository used by this class.
    private final CsvEventRepository eventRepository;
    // Stores the forecast service used by this class.
    private final ForecastService forecastService;
    // Stores the pricing service used by this class.
    private final DynamicPricingService pricingService;
    // Stores the auction service used by this class.
    private final AuctionService auctionService;
    // Stores the bid repository used by this class.
    private final CsvBidRepository bidRepository;
    // Stores the user repository used by this class.
    private final CsvUserRepository userRepository;
    private final PricingExporter pricingExporter = new PricingExporter();
    private final JdbcEventRepository jdbcEventRepository = new JdbcEventRepository(JDBC_URL);
    private final JdbcPriceRepository jdbcPriceRepository = new JdbcPriceRepository(JDBC_URL);
    private final JdbcAuctionRepository jdbcAuctionRepository = new JdbcAuctionRepository(JDBC_URL);
    private final Map<String, AuctionResult> auctionCache = new ConcurrentHashMap<>();
    private volatile List<ForecastResult> forecastCache = List.of();
    private volatile DynamicPricingService.PricingRunResult pricingCache;

    /**
     * Creates a FestQuantEngine with the values needed by this component.
     */
    public FestQuantEngine() {
        // Holds the events for this calculation.
        Path events = root.resolve("data/input/events.csv");
        // Holds the sales for this calculation.
        Path sales = root.resolve("data/input/ticket_sales_timeseries.csv");
        eventRepository = new CsvEventRepository(events);

        // Holds the pricing events for this calculation.
        CsvPricingEventRepository pricingEvents = new CsvPricingEventRepository(events);
        // Holds the sales repository for this calculation.
        CsvSalesRepository salesRepository = new CsvSalesRepository(sales);
        // Holds the forecaster for this calculation.
        TimeSeriesForecaster forecaster = new TimeSeriesForecaster();
        // Holds the nonlinear model for this calculation.
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

    /**
     * Handles the initialise step.
     */
    @PostConstruct
    public void initialise() {
        try {
            jdbcEventRepository.createTable();
            jdbcPriceRepository.createTable();
            jdbcAuctionRepository.createTable();
            // Uses event for the current item in the loop.
            for (Event event : events()) {
                jdbcEventRepository.save(event);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialise the embedded H2 database.", exception);
        }
        forecastCache = forecastService.forecastAll();
        refreshRecommendations();
    }

    /**
     * Handles the events step.
     */
    @Override
    public List<Event> events() {
        return eventRepository.findAllEvents();
    }

    /**
     * Handles the forecasts step.
     */
    @Override
    public List<ForecastResult> forecasts() {
        if (forecastCache.isEmpty()) {
            forecastCache = forecastService.forecastAll();
        }
        return forecastCache;
    }

    /**
     * Recommends ations.
     */
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
        // Holds the output for this calculation.
        Path output = root.resolve("data/output");
        pricingExporter.exportRecommendationsJson(pricingCache.getRecommendations(), output.resolve("price_recommendations.json"));
        pricingExporter.exportRecommendationsCsv(pricingCache.getRecommendations(), output.resolve("price_recommendations.csv"));
        pricingExporter.exportSimulationCsv(pricingCache.getSimulations(), output.resolve("pricing_simulation_details.csv"));
        pricingExporter.exportReport(pricingCache.getRecommendations(), output.resolve("dynamic_pricing_report.txt"));
        try {
            // Uses recommendation for the current item in the loop.
            for (PriceRecommendation recommendation : pricingCache.getRecommendations()) {
                jdbcPriceRepository.save(recommendation);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not persist price recommendations.", exception);
        }
        return pricingCache.getRecommendations();
    }

    /**
     * Handles the simulations step.
     */
    @Override
    public List<PriceSimulationRow> simulations(String eventId) {
        if (pricingCache == null) {
            refreshRecommendations();
        }
        return pricingCache.getSimulations().stream()
                .filter(row -> row.getEventId().equals(eventId))
                .toList();
    }

    /**
     * Handles the auction bids step.
     */
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

    /**
     * Runs auction.
     */
    @Override
    public AuctionResult runAuction(String eventId) {
        // Holds the recommended price for this calculation.
        double recommendedPrice = recommendations().stream()
                .filter(item -> item.getEventId().equals(eventId))
                .mapToDouble(PriceRecommendation::getRecommendedPrice)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No price recommendation for " + eventId));
        // Holds the result for this calculation.
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

    /**
     * Runs vickrey auction.
     */
    @Override
    public AuctionResult runVickreyAuction(String eventId) {
        // Holds the recommended price for this calculation.
        double recommendedPrice = recommendations().stream()
                .filter(item -> item.getEventId().equals(eventId))
                .mapToDouble(PriceRecommendation::getRecommendedPrice)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No price recommendation for " + eventId));
        // Holds the result for this calculation.
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

    /**
     * Handles the auction result step.
     */
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
        // Holds the named winners for this calculation.
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
        // Holds the events for this calculation.
        List<Event> events = events();
        // Holds the prices for this calculation.
        List<PriceRecommendation> prices = recommendations();
        // Holds the projected revenue for this calculation.
        double projectedRevenue = prices.stream().mapToDouble(PriceRecommendation::getExpectedRevenue).sum();
        // Holds the average lift for this calculation.
        double averageLift = prices.stream()
                .mapToDouble(item -> (item.getRecommendedPrice() / item.getBasePrice() - 1.0) * 100.0)
                .average().orElse(0.0);
        // Holds the rising for this calculation.
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
