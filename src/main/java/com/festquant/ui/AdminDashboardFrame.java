package com.festquant.ui;

import com.festquant.domain.AuctionResult;
import com.festquant.domain.Event;
import com.festquant.domain.PriceRecommendation;
import com.festquant.model.JavaNonlinearModelAdapter;
import com.festquant.model.TimeSeriesForecaster;
import com.festquant.pricing.RevenueMaximizingPricingStrategy;
import com.festquant.repository.CsvBidRepository;
import com.festquant.repository.CsvEventRepository;
import com.festquant.repository.CsvPricingEventRepository;
import com.festquant.repository.CsvSalesRepository;
import com.festquant.service.AuctionService;
import com.festquant.service.DataValidationService;
import com.festquant.service.DynamicPricingService;
import com.festquant.service.ExportService;
import com.festquant.util.JsonExporter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.List;

public class AdminDashboardFrame extends JFrame {
    private final CsvEventRepository eventRepository;
    private final AuctionService auctionService;
    private final DynamicPricingService pricingService;
    private final DefaultTableModel eventTableModel;
    private final JTextArea outputArea;

    public AdminDashboardFrame() {
        Path root = Path.of("").toAbsolutePath();
        this.eventRepository = new CsvEventRepository(root.resolve("data/input/events.csv"));
        this.auctionService = new AuctionService(
                eventRepository,
                new CsvBidRepository(root.resolve("data/input/premium_bids.csv"), new DataValidationService()),
                new ExportService(new JsonExporter())
        );
        JavaNonlinearModelAdapter nonlinearModel = new JavaNonlinearModelAdapter();
        nonlinearModel.loadFromPythonModel(root.resolve("models/nonlinear_demand_model.json"));
        this.pricingService = new DynamicPricingService(
                new CsvPricingEventRepository(root.resolve("data/input/events.csv")),
                new CsvSalesRepository(root.resolve("data/input/ticket_sales_timeseries.csv")),
                new TimeSeriesForecaster(),
                new RevenueMaximizingPricingStrategy(nonlinearModel)
        );
        this.eventTableModel = new DefaultTableModel(new Object[]{"Event ID", "Name", "Category", "Base Price", "Premium Seats"}, 0);
        this.outputArea = new JTextArea(8, 60);

        setTitle("FestQuant Admin Dashboard");
        setSize(900, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildLayout();
    }

    private void buildLayout() {
        JTable eventTable = new JTable(eventTableModel);
        add(new JScrollPane(eventTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 5, 8, 8));
        JButton loadEvents = new JButton("Load Events");
        JButton runForecast = new JButton("Run Forecast");
        JButton recommendPrices = new JButton("Recommend Prices");
        JButton runAuction = new JButton("Run Auction");
        JButton exportResults = new JButton("Export Results");

        buttons.add(loadEvents);
        buttons.add(runForecast);
        buttons.add(recommendPrices);
        buttons.add(runAuction);
        buttons.add(exportResults);
        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        loadEvents.addActionListener(event -> loadEvents());
        runForecast.addActionListener(event -> runPricingPipeline(true));
        recommendPrices.addActionListener(event -> runPricingPipeline(false));
        runAuction.addActionListener(event -> runAuction("E001"));
        exportResults.addActionListener(event -> exportAuction("E001"));
    }

    /**
     * SwingWorker keeps model execution off the Event Dispatch Thread and
     * demonstrates safe background work in the desktop UI.
     */
    private void runPricingPipeline(boolean forecastOnly) {
        outputArea.setText("Running integrated forecast and pricing pipeline...");
        new SwingWorker<DynamicPricingService.PricingRunResult, Void>() {
            @Override
            protected DynamicPricingService.PricingRunResult doInBackground() {
                return pricingService.recommendAllPrices();
            }

            @Override
            protected void done() {
                try {
                    List<PriceRecommendation> recommendations = get().getRecommendations();
                    StringBuilder summary = new StringBuilder(
                            forecastOnly ? "24-hour demand forecasts\n" : "Dynamic price recommendations\n");
                    for (PriceRecommendation item : recommendations) {
                        if (forecastOnly) {
                            summary.append(item.getEventId()).append(": ")
                                    .append(String.format("%.0f tickets (%s)%n",
                                            item.getForecastDemand(), item.getTrendLabel()));
                        } else {
                            summary.append(item.getEventId()).append(": Rs ")
                                    .append(String.format("%.0f -> %.0f%n",
                                            item.getBasePrice(), item.getRecommendedPrice()));
                        }
                    }
                    outputArea.setText(summary.toString());
                } catch (Exception exception) {
                    outputArea.setText("Pipeline failed: " + exception.getMessage());
                }
            }
        }.execute();
    }

    private void loadEvents() {
        eventTableModel.setRowCount(0);
        List<Event> events = eventRepository.findAllEvents();
        for (Event event : events) {
            eventTableModel.addRow(new Object[]{
                    event.getEventId(),
                    event.getEventName(),
                    event.getCategory(),
                    event.getBasePrice(),
                    event.getPremiumSeats()
            });
        }
        outputArea.setText("Loaded " + events.size() + " events from CSV.");
    }

    private void runAuction(String eventId) {
        AuctionResult result = auctionService.runAuction(eventId);
        outputArea.setText("Auction complete for " + eventId
                + "\nWinners: " + result.getWinners().stream().map(winner -> winner.getUserId()).toList()
                + "\nClearing price: Rs " + result.getClearingPrice()
                + "\nReserve price: Rs " + result.getReservePrice()
                + "\nFinal payment: Rs " + result.getFinalPayment()
                + "\n" + result.getExplanation());
    }

    private void exportAuction(String eventId) {
        AuctionResult result = auctionService.runAuction(eventId);
        Path outputPath = Path.of("").toAbsolutePath().resolve("data/output/auction_results.json");
        auctionService.exportAuctionResult(result, outputPath);
        outputArea.setText("Exported auction result to " + outputPath);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboardFrame().setVisible(true));
    }
}
