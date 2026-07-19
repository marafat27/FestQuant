/*
 * Runs the FestQuant browser dashboard and connects it to the REST and live SSE APIs.
 * The functions below keep rendering, API access and user actions in separate small steps.
 */

// Shared browser state. Keeping it in one object makes every view use the same data.
const state = {
  summary: {},                     // Totals shown on the overview cards.
  events: [],                      // Event records returned by the backend.
  forecasts: [],                   // Latest demand forecasts for every event.
  prices: [],                      // Current model-based price recommendations.
  selectedView: "overview",        // Sidebar page currently visible.
  selectedLiveEvent: null,         // Event selected in the live chart.
  liveSnapshots: {},               // Most recent SSE snapshot keyed by event ID.
  liveHistory: {},                 // Last 24 live points used by the chart.
  liveStatus: { running: true, scenario: "NORMAL" }, // Feed state and demand regime.
  liveSource: null,                // Active Server-Sent Events connection.
  liveRenderPending: false         // Prevents duplicate animation-frame renders.
};

// Holds the venue names used in this browser step.
const venueNames = Object.freeze({
  V001: "Main Arena",
  V002: "Auditorium",
  V003: "Open Air Stage",
  V004: "Convention Hall",
  V005: "Amphitheatre"
});

// Finds one HTML element by its ID.
const byId = (id) => document.getElementById(id);
// Formats numeric values as whole Indian rupees.
const money = (value) => new Intl.NumberFormat("en-IN", {
  style: "currency", currency: "INR", maximumFractionDigits: 0
}).format(value || 0);
// Formats ordinary whole numbers with Indian digit grouping.
const number = (value) => new Intl.NumberFormat("en-IN", { maximumFractionDigits: 0 }).format(value || 0);
// Converts backend labels such as RISING into readable title text.
const title = (value) => value ? value.charAt(0) + value.slice(1).toLowerCase() : "Unknown";

// Calls a JSON REST endpoint and turns non-success responses into clear errors.
async function api(path, options = {}) {
  // Holds the response used in this browser step.
  const response = await fetch(path, { headers: { "Accept": "application/json" }, ...options });
  if (!response.ok) {
    // Holds the body used in this browser step.
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message || `Request failed (${response.status})`);
  }
  return response.json();
}

// Shows a short success or error message at the top of the page.
function notify(message, isError = false) {
  // Holds the notice used in this browser step.
  const notice = byId("notice");
  notice.textContent = message;
  notice.className = `notice show${isError ? " error" : ""}`;
  clearTimeout(notice.timer);
  notice.timer = setTimeout(() => notice.className = "notice", 3200);
}

// Loads all.
async function loadAll() {
  try {
    const [summary, events, forecasts, prices, liveSnapshots, liveStatus] = await Promise.all([
      api("/api/dashboard/summary"),
      api("/api/events"),
      api("/api/forecast"),
      api("/api/pricing/recommendations"),
      api("/api/live/snapshots"),
      api("/api/live/status")
    ]);
    Object.assign(state, { summary, events, forecasts, prices, liveStatus });
    ingestLiveSnapshots(liveSnapshots);
    renderAll();
    connectLiveStream();
    byId("updated-time").textContent = new Date().toLocaleTimeString("en-IN", {
      hour: "2-digit", minute: "2-digit"
    });
  } catch (error) {
    notify(error.message, true);
  }
}

// Handles the event for step.
function eventFor(id) {
  return state.events.find(event => event.eventId === id) || { eventName: "Festival Event", category: "Event" };
}
// Handles the bidder name step.
function bidderName(winner) { return winner?.bidderName || winner?.userId || "No winner"; }
// Handles the venue name step.
function venueName(id) { return venueNames[id] || "Festival Venue"; }
// Handles the event initials step.
function eventInitials(name) {
  return name.split(/\s+/).slice(0, 2).map(word => word[0]).join("").toUpperCase();
}

// Renders metrics.
function renderMetrics() {
  byId("metric-revenue").textContent = money(state.summary.projectedRevenue);
  byId("metric-events").textContent = number(state.summary.eventCount);
  // Holds the live items used in this browser step.
  const liveItems = Object.values(state.liveSnapshots);
  // Holds the average lift used in this browser step.
  const averageLift = liveItems.length
    ? liveItems.reduce((sum, item) => sum + item.priceChangePercent, 0) / liveItems.length
    : Number(state.summary.averagePriceLift || 0);
  // Holds the rising used in this browser step.
  const rising = liveItems.length
    ? liveItems.filter(item => item.trend === "RISING").length
    : state.summary.risingEvents || 0;
  byId("metric-lift").textContent = `${averageLift >= 0 ? "+" : ""}${averageLift.toFixed(1)}%`;
  byId("metric-rising").textContent = `${rising} / ${state.summary.eventCount || 0}`;
}

// Renders revenue chart.
function renderRevenueChart() {
  // Holds the max used in this browser step.
  const max = Math.max(...state.prices.flatMap(item => [item.basePrice, item.recommendedPrice]), 1);
  byId("revenue-chart").innerHTML = state.prices.map(item => `
    <div class="bar-group" title="${eventFor(item.eventId).eventName}: ${money(item.recommendedPrice)}">
      <div class="bars">
        <i class="bar" style="height:${Math.max(8, item.basePrice / max * 100)}%"></i>
        <i class="bar rec" style="height:${Math.max(8, item.recommendedPrice / max * 100)}%"></i>
      </div>
      <span class="bar-label">${eventFor(item.eventId).eventName}</span>
    </div>`).join("");
}

// Renders momentum.
function renderMomentum() {
  // Holds the rows used in this browser step.
  const rows = [...state.forecasts].sort((a, b) => b.forecastNext24Hours - a.forecastNext24Hours).slice(0, 5);
  // Holds the max used in this browser step.
  const max = Math.max(...rows.map(item => item.forecastNext24Hours), 1);
  byId("momentum-list").innerHTML = rows.map(item => `
    <div class="momentum-row">
      <span class="momentum-name">${eventFor(item.eventId).eventName}</span>
      <span class="momentum-value">${number(item.forecastNext24Hours)}</span>
      <span class="momentum-track"><i style="width:${Math.max(5, item.forecastNext24Hours / max * 100)}%"></i></span>
    </div>`).join("");
}

// Renders overview table.
function renderOverviewTable() {
  byId("overview-table").innerHTML = state.prices.slice(0, 5).map(item => {
    // Holds the event used in this browser step.
    const event = eventFor(item.eventId);
    // Holds the live used in this browser step.
    const live = state.liveSnapshots[item.eventId];
    return `<tr>
      <td><span class="event-cell"><i class="event-tag">${eventInitials(event.eventName)}</i>${event.eventName}</span></td>
      <td><span class="signal ${(live?.trend || item.trendLabel).toLowerCase()}">${title(live?.trend || item.trendLabel)}</span></td>
      <td>${money(item.basePrice)}</td><td><strong>${money(live?.livePrice || item.recommendedPrice)}</strong></td>
      <td>${money(item.expectedRevenue)}</td><td><span class="status-badge">${live ? "Updating live" : "Model ready"}</span></td>
    </tr>`;
  }).join("");
}

// Renders events.
function renderEvents(filter = "") {
  // Holds the query used in this browser step.
  const query = filter.trim().toLowerCase();
  // Holds the items used in this browser step.
  const items = state.events.filter(event =>
    `${event.eventName} ${event.category} ${event.eventId}`.toLowerCase().includes(query)
  );
  byId("event-grid").innerHTML = items.map(event => `
    <article class="event-card">
      <div class="event-card-top"><span class="category-pill">${event.category}</span><span class="event-venue">${venueName(event.venueId)}</span></div>
      <h3>${event.eventName}</h3>
      <p class="event-meta">${new Date(event.eventDateTime).toLocaleString("en-IN", {
        day: "numeric", month: "short", hour: "numeric", minute: "2-digit"
      })} · ${venueName(event.venueId)}</p>
      <div class="event-stats">
        <span><small>Capacity</small><strong>${number(event.capacity)}</strong></span>
        <span><small>Base price</small><strong>${money(event.basePrice)}</strong></span>
        <span><small>Premium</small><strong>${number(event.premiumSeats)}</strong></span>
      </div>
    </article>`).join("");
}

// Renders pricing.
function renderPricing() {
  byId("pricing-list").innerHTML = state.prices.map(item => {
    // Holds the event used in this browser step.
    const event = eventFor(item.eventId);
    // Holds the live used in this browser step.
    const live = state.liveSnapshots[item.eventId];
    return `<article class="pricing-card">
      <div><h3>${event.eventName}</h3><p>${event.category} · ${venueName(event.venueId)}</p></div>
      <div><p>${live ? "Live price" : "Recommended move"}</p><div class="price-shift"><span class="old-price">${money(item.basePrice)}</span><span>→</span><strong class="new-price">${money(live?.livePrice || item.recommendedPrice)}</strong></div></div>
      <div><p>${live ? "Rolling demand" : "24h demand / revenue"}</p><strong>${live ? `${live.rollingDemand} tickets / pulse` : `${number(item.forecastDemand)} · ${money(item.expectedRevenue)}`}</strong></div>
      <p class="explanation">${item.explanation}</p>
    </article>`;
  }).join("");
}

// Handles the ingest live snapshots step.
function ingestLiveSnapshots(items) {
  items.forEach(ingestLiveSnapshot);
  if (!state.selectedLiveEvent && items.length) {
    state.selectedLiveEvent = items[0].eventId;
  }
}

// Handles the ingest live snapshot step.
function ingestLiveSnapshot(snapshot) {
  state.liveSnapshots[snapshot.eventId] = snapshot;
  state.liveStatus.running = snapshot.running;
  state.liveStatus.scenario = snapshot.scenario;
  // Holds the history used in this browser step.
  const history = state.liveHistory[snapshot.eventId] || [];
  if (!history.length || history[history.length - 1].sequence !== snapshot.sequence) {
    history.push({
      sequence: snapshot.sequence,
      demand: snapshot.rollingDemand,
      price: snapshot.livePrice
    });
    if (history.length > 24) history.shift();
  }
  state.liveHistory[snapshot.eventId] = history;
}

// Handles the schedule live render step.
function scheduleLiveRender() {
  if (state.liveRenderPending) return;
  state.liveRenderPending = true;
  window.requestAnimationFrame(() => {
    state.liveRenderPending = false;
    renderLive();
    renderMetrics();
    if (state.selectedView === "overview") renderOverviewTable();
    if (state.selectedView === "pricing") renderPricing();
  });
}

// Renders live.
function renderLive() {
  // Holds the snapshots used in this browser step.
  const snapshots = Object.values(state.liveSnapshots).sort((a, b) => a.eventName.localeCompare(b.eventName));
  if (!snapshots.length) return;
  if (!state.selectedLiveEvent) state.selectedLiveEvent = snapshots[0].eventId;

  // Holds the select used in this browser step.
  const select = byId("live-event-select");
  // Holds the previous value used in this browser step.
  const previousValue = state.selectedLiveEvent;
  select.innerHTML = snapshots.map(item =>
    `<option value="${item.eventId}">${item.eventName}</option>`
  ).join("");
  select.value = previousValue;

  // Holds the selected used in this browser step.
  const selected = state.liveSnapshots[state.selectedLiveEvent] || snapshots[0];
  state.selectedLiveEvent = selected.eventId;
  byId("live-latest-demand").textContent = number(selected.latestDemand);
  byId("live-rolling-demand").textContent = selected.rollingDemand.toFixed(1);
  byId("live-current-price").textContent = money(selected.livePrice);
  byId("live-price-change").textContent =
    `${selected.priceChangePercent >= 0 ? "+" : ""}${selected.priceChangePercent.toFixed(1)}% vs base`;
  // Holds the trend used in this browser step.
  const trend = byId("live-trend-badge");
  trend.textContent = title(selected.trend);
  trend.className = `signal ${selected.trend.toLowerCase()}`;

  byId("live-event-list").innerHTML = snapshots.map(item => `
    <div class="live-event-row ${item.eventId === selected.eventId ? "selected" : ""}" data-live-event="${item.eventId}">
      <button type="button" data-select-live="${item.eventId}">
        <strong>${item.eventName}</strong><small>${title(item.trend)} demand</small>
      </button>
      <div class="live-demand-number"><strong>${item.rollingDemand.toFixed(1)}</strong><small>Demand</small></div>
      <div class="live-price-number"><strong>${money(item.livePrice)}</strong><small>${item.priceChangePercent >= 0 ? "+" : ""}${item.priceChangePercent.toFixed(1)}%</small></div>
    </div>`).join("");

  document.querySelectorAll("[data-select-live]").forEach(button =>
    button.addEventListener("click", () => {
      state.selectedLiveEvent = button.dataset.selectLive;
      renderLive();
    })
  );
  renderLiveChart(state.liveHistory[selected.eventId] || []);
  renderLiveControls();
}

// Renders live chart.
function renderLiveChart(history) {
  byId("live-demand-line").setAttribute("points", linePoints(history.map(item => item.demand)));
  byId("live-price-line").setAttribute("points", linePoints(history.map(item => item.price)));
}

// Scales a list of values into SVG x,y coordinates for the live chart.
function linePoints(values) {
  if (!values.length) return "";
  // Holds the width used in this browser step.
  const width = 700;
  // Holds the height used in this browser step.
  const height = 215;
  // Holds the minimum used in this browser step.
  const minimum = Math.min(...values);
  // Holds the maximum used in this browser step.
  const maximum = Math.max(...values);
  // Range is never zero, which avoids division by zero for a flat series.
  const range = Math.max(1, maximum - minimum);
  return values.map((value, index) => {
    // X spreads each observation evenly across the 700-pixel chart width.
    const x = values.length === 1 ? width / 2 : index / (values.length - 1) * width;
    // Y normalizes the value into the plot area and flips it for SVG coordinates.
    const y = height - ((value - minimum) / range * (height - 28) + 14);
    return `${x.toFixed(1)},${y.toFixed(1)}`;
  }).join(" ");
}

// Renders live controls.
function renderLiveControls() {
  // Holds the scenario labels used in this browser step.
  const scenarioLabels = {
    COOLING: "Demand drop",
    NORMAL: "Normal demand",
    SURGE: "Demand surge"
  };
  byId("live-scenario-label").textContent = scenarioLabels[state.liveStatus.scenario] || "Normal demand";
  document.querySelectorAll("[data-scenario]").forEach(button =>
    button.classList.toggle("active", button.dataset.scenario === state.liveStatus.scenario && state.liveStatus.running)
  );
  byId("live-pause-button").textContent = state.liveStatus.running ? "Pause" : "Resume";
}

// Handles the connect live stream step.
function connectLiveStream() {
  if (state.liveSource) state.liveSource.close();
  // Holds the source used in this browser step.
  const source = new EventSource("/api/live/stream");
  state.liveSource = source;
  source.onopen = () => {
    byId("live-connection-label").textContent = "Streaming";
    byId("live-connection-label").parentElement.classList.add("connected");
  };
  source.onerror = () => {
    byId("live-connection-label").textContent = "Reconnecting";
    byId("live-connection-label").parentElement.classList.remove("connected");
  };
  source.addEventListener("snapshot", event => {
    ingestLiveSnapshots(JSON.parse(event.data));
    scheduleLiveRender();
  });
  source.addEventListener("demand-update", event => {
    ingestLiveSnapshot(JSON.parse(event.data));
    scheduleLiveRender();
  });
}

// Updates live scenario.
async function setLiveScenario(scenario) {
  try {
    state.liveStatus = await api(`/api/live/scenario/${scenario}`, { method: "POST" });
    renderLiveControls();
    notify(`${title(scenario)} scenario is now running.`);
  } catch (error) {
    notify(error.message, true);
  }
}

// Handles the toggle live feed step.
async function toggleLiveFeed() {
  try {
    // Holds the action used in this browser step.
    const action = state.liveStatus.running ? "pause" : "start";
    state.liveStatus = await api(`/api/live/${action}`, { method: "POST" });
    renderLiveControls();
    notify(state.liveStatus.running ? "Live demand resumed." : "Live demand paused.");
  } catch (error) {
    notify(error.message, true);
  }
}

// Renders auction control.
function renderAuctionControl() {
  // Holds the eligible used in this browser step.
  const eligible = state.events;
  // Holds the select used in this browser step.
  const select = byId("auction-event");
  select.innerHTML = eligible.map(event => `<option value="${event.eventId}">${event.eventName}</option>`).join("");
  updateAuctionContext();
}

// Refreshes the reserve explanation and loads bids for the selected event.
function updateAuctionContext() {
  // Holds the event used in this browser step.
  const event = eventFor(byId("auction-event").value);
  // Holds the price used in this browser step.
  const price = state.prices.find(item => item.eventId === event.eventId);
  if (!event.eventId) return;
  // Displayed reserve = max(1.5*base price, 1.25*recommended price).
  const reserve = Math.max(event.basePrice * 1.5, (price?.recommendedPrice || 0) * 1.25);
  byId("auction-context").innerHTML = `
    <strong>1 showcase premium seat</strong><br>
    Dynamic reserve ${money(reserve)}<br>
    Highest eligible bid wins and pays the second-highest eligible bid or reserve.`;
  resetAuctionResult();
  loadAuctionBids(event.eventId);
}

// Loads auction bids.
async function loadAuctionBids(eventId) {
  byId("auction-bid-list").innerHTML = '<p class="bid-loading">Loading submitted bids…</p>';
  try {
    // Holds the bids used in this browser step.
    const bids = await api(`/api/auction/bids/${eventId}`);
    if (byId("auction-event").value !== eventId) return;
    byId("auction-bid-count").textContent = `${bids.length} ${bids.length === 1 ? "bid" : "bids"}`;
    byId("auction-bid-list").innerHTML = bids.length ? bids.map((bid, index) => `
      <div class="auction-bid">
        <span class="bid-position">${index + 1}</span>
        <div><strong>${bid.bidderName}</strong><small>${new Date(bid.bidTime).toLocaleTimeString("en-IN", { hour: "2-digit", minute: "2-digit" })}</small></div>
        <span class="bid-amount">${money(bid.bidAmount)}</span>
      </div>`).join("") : '<p class="bid-loading">No submitted bids for this event.</p>';
  } catch (error) {
    byId("auction-bid-list").innerHTML = '<p class="bid-loading">Bids could not be loaded.</p>';
    notify(error.message, true);
  }
}

// Handles the reset auction result step.
function resetAuctionResult() {
  byId("auction-empty").hidden = false;
  byId("auction-result-content").hidden = true;
}

// Renders all.
function renderAll() {
  renderMetrics();
  renderRevenueChart();
  renderMomentum();
  renderOverviewTable();
  renderEvents(byId("event-search").value);
  renderPricing();
  renderLive();
  renderAuctionControl();
}

// Handles the switch view step.
function switchView(view) {
  state.selectedView = view;
  document.querySelectorAll(".nav-item").forEach(item =>
    item.classList.toggle("active", item.dataset.view === view)
  );
  document.querySelectorAll(".view").forEach(item =>
    item.classList.toggle("active", item.id === `${view}-view`)
  );
  byId("breadcrumb").textContent = title(view);
  // Holds the headings used in this browser step.
  const headings = {
    overview: "Good evening, Admin.",
    live: "Watch the market move.",
    events: "Your festival programme.",
    pricing: "Make every seat count.",
    auction: "Allocate premium seats fairly."
  };
  byId("page-title").textContent = headings[view];
  window.scrollTo({ top: 0, behavior: "smooth" });
}

// Handles the recompute step.
async function recompute() {
  // Holds the buttons used in this browser step.
  const buttons = [byId("refresh-button"), byId("recompute-button")];
  buttons.forEach(button => { button.disabled = true; button.textContent = "Computing…"; });
  try {
    state.prices = await api("/api/pricing/recommend-all", { method: "POST" });
    state.summary = await api("/api/dashboard/summary");
    state.forecasts = await api("/api/forecast");
    renderAll();
    byId("updated-time").textContent = new Date().toLocaleTimeString("en-IN", {
      hour: "2-digit", minute: "2-digit"
    });
    notify("Forecast and pricing pipeline completed.");
  } catch (error) {
    notify(error.message, true);
  } finally {
    byId("refresh-button").disabled = false;
    byId("refresh-button").textContent = "Run pipeline";
    byId("recompute-button").disabled = false;
    byId("recompute-button").textContent = "Recompute prices";
  }
}

// Runs auction.
async function runAuction() {
  // Holds the event id used in this browser step.
  const eventId = byId("auction-event").value;
  // Holds the button used in this browser step.
  const button = byId("run-auction-button");
  button.disabled = true;
  button.textContent = "Running Vickrey model…";
  try {
    // Holds the result used in this browser step.
    const result = await api(`/api/auction/run-vickrey/${eventId}`, { method: "POST" });
    byId("auction-empty").hidden = true;
    // Holds the content used in this browser step.
    const content = byId("auction-result-content");
    content.hidden = false;
    // Holds the winner used in this browser step.
    const winner = result.winners[0];
    content.innerHTML = `
      <div class="panel-head"><div><p class="section-kicker">Vickrey auction complete</p><h3>${eventFor(result.eventId).eventName}</h3></div><span class="status-badge">Second-price allocation</span></div>
      <div class="auction-summary">
        <div><small>Winning bid</small><strong>${winner ? money(winner.bidAmount) : "No winner"}</strong></div>
        <div><small>Reserve price</small><strong>${money(result.reservePrice)}</strong></div>
        <div><small>Winner pays</small><strong>${winner ? money(winner.finalPayment) : "—"}</strong></div>
      </div>
      <p class="section-kicker">Declared winner</p>
      <div class="winner-list">${winner ? `
        <div class="winner"><span class="winner-rank">1</span><span>${bidderName(winner)}</span><small>Bid ${money(winner.bidAmount)} · Pays ${money(winner.finalPayment)}</small></div>` : "<p>No bid met the dynamic reserve price.</p>"}
      </div>
      <p class="auction-explanation">${result.explanation}</p>`;
    notify(`Vickrey auction completed for ${eventFor(eventId).eventName}.`);
  } catch (error) {
    notify(error.message, true);
  } finally {
    button.disabled = false;
    button.textContent = "Run Vickrey auction";
  }
}

document.querySelectorAll(".nav-item").forEach(item =>
  item.addEventListener("click", () => switchView(item.dataset.view))
);
document.querySelectorAll("[data-go]").forEach(item =>
  item.addEventListener("click", () => switchView(item.dataset.go))
);
document.querySelectorAll("[data-scenario]").forEach(button =>
  button.addEventListener("click", () => setLiveScenario(button.dataset.scenario))
);
byId("event-search").addEventListener("input", event => renderEvents(event.target.value));
byId("live-event-select").addEventListener("change", event => {
  state.selectedLiveEvent = event.target.value;
  renderLive();
});
byId("live-pause-button").addEventListener("click", toggleLiveFeed);
byId("auction-event").addEventListener("change", updateAuctionContext);
byId("run-auction-button").addEventListener("click", runAuction);
byId("refresh-button").addEventListener("click", recompute);
byId("recompute-button").addEventListener("click", recompute);
window.addEventListener("beforeunload", () => state.liveSource?.close());
loadAll();
