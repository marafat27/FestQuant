package com.festquant.stream;

/** Demand regimes available to the compressed-time demonstration feed. */
public enum DemandScenario {
    COOLING(0.45),
    NORMAL(1.0),
    SURGE(1.85);

    private final double multiplier;

    DemandScenario(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }
}
