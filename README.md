# FestQuant: Festival Ticket Demand, Pricing and Auction System

FestQuant is a Java OOP application for managing festival ticket revenue. It combines demand forecasting, nonlinear regression-based pricing, real-time demand simulation, and premium-seat auctions in one Spring Boot web application. The final interface is available through a browser dashboard, while the backend remains organized around reusable Java classes, interfaces and services.

## Main Features

- Loads events, users, ticket-sales history, price history and premium bids from CSV files.
- Forecasts short-term demand for each event using hourly ticket-sales history.
- Uses a nonlinear logistic regression model to estimate how demand changes with views, wishlists, sold ratio, urgency, slot popularity and price.
- Recommends prices by simulating candidate ticket prices and choosing the best revenue outcome within fairness and min/max limits.
- Simulates live demand every few seconds so the frontend can show changing demand and prices.
- Runs premium-seat auctions using Vickrey second-price logic and a multi-unit uniform-price auction option.
- Exposes REST APIs and a professional web frontend for dashboard, pricing, live market and auction workflows.
- Includes JUnit tests for repositories, validation, forecasting, pricing, export and auction rules.

## Problem Definition

Festival organisers need one reliable operations system that can decide ticket prices and premium-seat allocation as demand changes. The project solves three connected problems:

1. Estimate demand from historical sales and engagement data.
2. Recommend fair prices that improve expected revenue without exceeding stated price limits.
3. Allocate scarce premium seats through a transparent auction mechanism.

Inputs:

- Event schedule, venue capacity, base/min/max ticket prices and premium-seat counts.
- Hourly sales history, views, wishlists and user activity.
- Participant records and sealed premium-seat bids.
- Trained nonlinear regression coefficients.

Outputs:

- Demand forecasts and demand trend labels.
- Price recommendations with expected demand, expected revenue and explanations.
- Live demand/price snapshots for the frontend.
- Auction winners, winning bids, reserve prices and final payments.
- JSON/CSV output files for inspection and integration.

Success metrics:

- The app starts and runs from a Maven build.
- Unit tests pass with no failures.
- Prices stay inside min/max and fairness limits.
- Auction winners/payments follow second-price rules.
- The frontend shows named users/events, live updates and clear explanations.

## Build and Run Instructions

Requirements:

- Java 17 or newer
- Maven 3.9+ or the included Maven wrapper

From the project root:

```bash
chmod +x mvnw
./mvnw test
./mvnw spring-boot:run
```

On macOS, run the commands in Terminal from the folder that contains `pom.xml`.
The first wrapper run may download Maven, so an internet connection may be
needed once. Stop the application with `Control+C`.

Open:

```text
http://localhost:8080
```

To create and run the executable JAR:

```bash
./mvnw package
java -jar target/festquant-1.0.0.jar
```

On Windows:

```powershell
mvnw.cmd test
mvnw.cmd spring-boot:run
```

## Project Structure

```text
data/input/                         Source CSV datasets
data/output/                        Generated recommendations and auction exports
models/nonlinear_demand_model.json  Saved regression coefficients and metrics
src/main/java/com/festquant/
  auction/                          Auction interfaces and algorithms
  controller/                       REST API controllers
  domain/                           Core business entities
  exception/                        Custom error classes
  model/                            Forecasting and regression adapter classes
  pricing/                          Dynamic-pricing strategy classes
  repository/                       CSV and JDBC data-access classes
  service/                          Business workflow services and facade
  stream/                           Live demand/pricing simulation
  ui/                               Optional Swing dashboard
  util/                             CSV, JSON and pricing export helpers
src/main/resources/static/          Browser frontend
src/test/java/com/festquant/        Unit tests
```

## OOP Design and Java Concepts

### Encapsulation

Domain classes such as `Event`, `Bid`, `User`, `AuctionResult`, `ForecastResult` and `PriceRecommendation` keep their fields private and expose data through constructors and getters. This prevents unrelated parts of the system from directly changing business state.

### Inheritance

`User` is the parent type for `Participant` and `Admin`. This models the fact that both are users, while still allowing role-specific objects. The frontend and repositories can work with the common `User` type when the exact role is not important.

### Abstraction

The project uses interfaces such as:

- `Repository<T>` for data access
- `AuctionStrategy` for auction algorithms
- `PricingStrategy` for pricing algorithms
- `AnalyticsFacade` for the application service boundary

These interfaces hide implementation details. For example, controllers do not need to know whether event data came from CSV or H2; they call the facade.

### Polymorphism

`VickreyAuction` and `MultiUnitAuction` both implement `AuctionStrategy`. The service layer can run an auction through the common interface while each class applies a different rule. This is the main auction-related example of runtime polymorphism.

### Design Patterns

- Strategy pattern: pricing and auction algorithms are interchangeable.
- Repository pattern: CSV and JDBC data access are isolated from business logic.
- Facade pattern: `FestQuantEngine` gives controllers a single entry point to forecasts, prices, auctions and dashboard summaries.
- MVC pattern: Spring controllers serve model data to the frontend views.
- Producer-consumer pattern: live ticket activity is generated, queued and consumed asynchronously.

### Java Tools Used

The code uses collections, streams, lambdas, comparators, generics, records, custom exceptions, JDBC, file I/O, JSON export, concurrency utilities, scheduled executors, blocking queues, concurrent maps, Server-Sent Events and JUnit 5. These satisfy the advanced Java feature requirement through multithreading, JDBC, serialization-style JSON export, collections, generics, lambdas, regex-based parsing and design patterns.

## Integration Mechanisms

The assignment requires clear integration with existing or simulated systems. FestQuant demonstrates this in four ways:

- REST APIs: Spring controllers expose events, forecasts, pricing, live market data and auction workflows.
- File exchange: CSV inputs are loaded from `data/input`, and JSON/CSV outputs are written to `data/output`.
- JDBC: the application stores events, price recommendations and auction results in an embedded H2 database when it runs.
- Simulated real-time stream: `LiveDemandService` uses `ScheduledExecutorService`, `ArrayBlockingQueue` and Server-Sent Events to mimic a live order/demand feed.

The `Run pipeline` button on the dashboard re-runs the integrated backend workflow: demand forecasting, price recomputation, dashboard metric refresh and output-file update. It shows the system recalculating decisions after data or model changes instead of displaying only stored results.

## Data Generation

The dataset represents a synthetic college-festival ticketing environment. It includes:

- 10 events across music, comedy, dance, drama, fashion and literary categories.
- Event capacity, base price, min price, max price, premium-seat count and slot popularity.
- Hourly ticket-sales observations for the 30 days before each event.
- User activity such as views, wishlists, clicks and registrations.
- Premium-seat bids from named participants.
- Price history used as a baseline for dynamic pricing.

The generated data follows realistic constraints:

- Event capacity stays within a sensible venue range.
- Base prices and min/max prices remain within configured ticketing limits.
- Sales accelerate as the event date approaches through an urgency factor.
- Popular events receive stronger views, wishlists and bids.
- Premium bids are above ordinary ticket prices because they represent better seats.

This gives the regression and pricing modules enough variation to demonstrate forecasting, price sensitivity and auction economics without using private real-world data.

## Regression Model

The demand model is a nonlinear logistic regression:

```text
DemandRatio = 1 / (1 + exp(-z))

z = beta0
  + beta1 * logViews
  + beta2 * wishlistRate
  + beta3 * soldRatio
  + beta4 * urgencyIndex
  + beta5 * slotPopularity
  - beta6 * priceIndex
```

Feature meanings:

- `logViews`: interest level after log-scaling page views.
- `wishlistRate`: fraction of viewers who showed stronger intent.
- `soldRatio`: how much capacity is already sold.
- `urgencyIndex`: how close the event is.
- `slotPopularity`: expected attractiveness of the event slot/category.
- `priceIndex`: candidate price divided by base price.

The model coefficients and diagnostics are stored in `models/nonlinear_demand_model.json`. `JavaNonlinearModelAdapter` reads that file so the Java pricing engine can use the trained coefficients directly at runtime.

## Dynamic Pricing Economics

`RevenueMaximizingPricingStrategy` evaluates candidate prices between the allowed lower and upper bounds. For each price it:

1. Builds regression features from the latest event state.
2. Predicts nonlinear demand with the logistic model.
3. Blends that prediction with the time-series forecast.
4. Limits expected tickets by remaining capacity.
5. Computes expected revenue as:

   ```text
   expectedRevenue = candidatePrice * expectedTickets
   ```

6. Chooses the candidate price with the highest expected revenue.

The final recommendation is constrained by:

- Event-level minimum and maximum ticket prices.
- A fairness cap of at most 20% above base price.
- A floor of 15% below base price to avoid unrealistic discounting.
- A conservative rule that avoids increasing prices when very little capacity has sold.

## Forecasting

`TimeSeriesForecaster` uses Holt-style level and trend smoothing over hourly ticket-sales intervals. This captures whether recent sales are rising, stable or falling. The forecast label is shown in the dashboard and is also used by the pricing explanation.

## Live Demand and Real-Time Price Demonstration

The Live Market screen demonstrates how prices would respond to new demand:

- `LiveDemandService` creates ticket-sale pulses every two seconds.
- Each pulse contains views, wishlists and ticket sales for every event.
- An `ArrayBlockingQueue` separates generation from processing.
- A consumer thread updates each event’s rolling demand window.
- `LivePricingPolicy` adjusts the historical recommended price using the live demand index.
- Spring Server-Sent Events stream snapshots to the browser.

Admin controls can switch the live market between demand drop, normal demand and demand surge. Prices still respect min/max and fairness rules.

## Auction Economics

Premium seats are allocated through auction mechanisms.

### Vickrey Second-Price Auction

For a single premium seat:

1. Bids below the dynamic reserve price are rejected.
2. The highest eligible bidder wins.
3. The winner pays the greater of:

   ```text
   secondHighestEligibleBid
   reservePrice
   ```

This makes truthful bidding rational because the winner’s payment is determined by the next-best eligible bid, not by their own bid.

### Multi-Unit Uniform-Price Auction

For multiple premium seats:

1. Eligible bids are sorted from highest to lowest.
2. The top bidders win up to the available premium-seat count.
3. Winners pay a uniform clearing price based on the next eligible losing bid or the reserve price.

The dashboard focuses on the Vickrey flow: the admin first sees named bids, then clicks Run Vickrey Auction to declare the winner and payment.

## REST APIs

- `GET /api/events`
- `GET /api/dashboard/summary`
- `GET /api/forecast`
- `POST /api/forecast/run-all`
- `GET /api/pricing/recommendations`
- `POST /api/pricing/recommend-all`
- `GET /api/pricing/simulations/{eventId}`
- `GET /api/live/snapshots`
- `GET /api/live/status`
- `GET /api/live/stream`
- `POST /api/live/start`
- `POST /api/live/pause`
- `POST /api/live/scenario/{COOLING|NORMAL|SURGE}`
- `GET /api/auction/bids/{eventId}`
- `POST /api/auction/run-vickrey/{eventId}`
- `POST /api/auction/run/{eventId}`
- `GET /api/auction/results/{eventId}`

## Unit Tests

Run all tests with:

```bash
./mvnw test
```

The test suite checks:

- CSV bid loading and event filtering.
- Data validation for event ranges and invalid records.
- JSON exporting of auction results.
- Vickrey auction winner/payment rules.
- Multi-unit auction allocation and reserve-price behavior.
- Time-series forecasting output.
- Revenue-maximising pricing constraints.
- Live pricing response to demand changes and fairness bounds.

These tests are important because the project combines several moving parts: data loading, economics, forecasting, pricing rules, auction rules and live-market behavior. The tests confirm that core business rules continue to work after integration.

## Frontend

The web frontend is in `src/main/resources/static/`:

- `index.html` defines the dashboard sections.
- `styles.css` contains the responsive visual design.
- `app.js` calls the REST APIs, renders charts/tables and handles live updates.

The browser UI includes:

- Dashboard summary cards.
- Event and pricing tables using event names.
- Live demand and live price chart.
- Auction desk with named bids and Vickrey auction execution.
- Hover tooltips on navigation and action buttons explaining what each button does.

## Output Files

The application writes generated outputs under `data/output/`, including:

- `price_recommendations.json`
- `price_recommendations.csv`
- `pricing_simulation_details.csv`
- `dynamic_pricing_report.txt`
- `auction_results.json`

The embedded H2 database is created in `data/` when the application runs.

## Common Startup Issue

If port 8080 is already being used on macOS, find and stop the old process:

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
kill <PID>
```

Then start FestQuant again with `./mvnw spring-boot:run`. Alternatively, choose
another port temporarily with `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`.
