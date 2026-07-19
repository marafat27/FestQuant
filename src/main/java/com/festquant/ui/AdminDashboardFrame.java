/**
 * Contains the admin dashboard frame implementation used by FestQuant.
 */
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

/**
 * Represents the admin dashboard frame part of the FestQuant application.
 */
public class AdminDashboardFrame extends JFrame {
    // Stores the event repository used by this class.
    private final CsvEventRepository eventRepository;
    // Stores the auction service used by this class.
    private final AuctionService auctionService;
    // Stores the pricing service used by this class.
    private final DynamicPricingService pricingService;
    // Stores the event table model used by this class.
    private final DefaultTableModel eventTableModel;
    // Stores the output area used by this class.
    private final JTextArea outputArea;

    /**
     * Creates a AdminDashboardFrame with the values needed by this component.
     */
    public AdminDashboardFrame() {
        // Holds the root for this calculation.
        Path root = Path.of("").toAbsolutePath();
        this.eventRepository = new CsvEventRepository(root.resolve("data/input/events.csv"));
        this.auctionService = new AuctionService(
                eventRepository,
                new CsvBidRepository(root.resolve("data/input/premium_bids.csv"), new DataValidationService()),
                new ExportService(new JsonExporter())
        );
        // Holds the nonlinear model for this calculation.
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

    /**
     * Builds layout.
     */
    private void buildLayout() {
        // Holds the event table for this calculation.
        JTable eventTable = new JTable(eventTableModel);
        add(new JScrollPane(eventTable), BorderLayout.CENTER);

        // Holds the buttons for this calculation.
        JPanel buttons = new JPanel(new GridLayout(1, 5, 8, 8));
        // Holds the load events for this calculation.
        JButton loadEvents = new JButton("Load Events");
        // Holds the run forecast for this calculation.
        JButton runForecast = new JButton("Run Forecast");
        // Holds the recommend prices for this calculation.
        JButton recommendPrices = new JButton("Recommend Prices");
        // Holds the run auction for this calculation.
        JButton runAuction = new JButton("Run Auction");
        // Holds the export results for this calculation.
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
            /**
             * Handles the do in background step.
             */
            @Override
            protected DynamicPricingService.PricingRunResult doInBackground() {
                return pricingService.recommendAllPrices();
            }

            /**
             * Handles the done step.
             */
            @Override
            protected void done() {
                try {
                    // Holds the recommendations for this calculation.
                    List<PriceRecommendation> recommendations = get().getRecommendations();
                    // Holds the summary for this calculation.
                    StringBuilder summary = new StringBuilder(
                            forecastOnly ? "24-hour demand forecasts\n" : "Dynamic price recommendations\n");
                    // Uses item for the current item in the loop.
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

    /**
     * Loads events.
     */
    private void loadEvents() {
        eventTableModel.setRowCount(0);
        // Holds the events for this calculation.
        List<Event> events = eventRepository.findAllEvents();
        // Uses event for the current item in the loop.
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

    /**
     * Runs auction.
     */
    private void runAuction(String eventId) {
        // Holds the result for this calculation.
        AuctionResult result = auctionService.runAuction(eventId);
        outputArea.setText("Auction complete for " + eventId
                + "\nWinners: " + result.getWinners().stream().map(winner -> winner.getUserId()).toList()
                + "\nClearing price: Rs " + result.getClearingPrice()
                + "\nReserve price: Rs " + result.getReservePrice()
                + "\nFinal payment: Rs " + result.getFinalPayment()
                + "\n" + result.getExplanation());
    }

    /**
     * Exports auction.
     */
    private void exportAuction(String eventId) {
        // Holds the result for this calculation.
        AuctionResult result = auctionService.runAuction(eventId);
        // Holds the output path for this calculation.
        Path outputPath = Path.of("").toAbsolutePath().resolve("data/output/auction_results.json");
        auctionService.exportAuctionResult(result, outputPath);
        outputArea.setText("Exported auction result to " + outputPath);
    }

    /**
     * Handles the main step.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboardFrame().setVisible(true));
    }
}
