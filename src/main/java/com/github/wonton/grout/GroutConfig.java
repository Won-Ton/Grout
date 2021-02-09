package com.github.wonton.grout;

public class GroutConfig {

    public final boolean mergeDataPacks;
    public final boolean injectDefaults;

    GroutConfig(boolean mergeDataPacks, boolean injectDefaults) {
        this.mergeDataPacks = mergeDataPacks;
        this.injectDefaults = injectDefaults;
    }

    public boolean hasInjections() {
        return mergeDataPacks || injectDefaults;
    }

    @Override
    public String toString() {
        return "GroutConfig{" +
                "mergeDataPacks=" + mergeDataPacks +
                ", injectDefaults=" + injectDefaults +
                '}';
    }
}
