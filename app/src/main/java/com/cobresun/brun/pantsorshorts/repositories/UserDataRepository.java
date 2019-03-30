package com.cobresun.brun.pantsorshorts.repositories;

public interface UserDataRepository {

    float readUserThreshold();

    void writeUserThreshold(float threshold);

    long readLastTimeFetchedWeather();

    void writeLastTimeFetchedWeather(long time);

    float readLastFetchedTemp();

    void writeLastFetchedTemp(float temp);

    boolean isFirstTimeLaunching();

    boolean isNightMode();

    void writeNightMode(boolean nightMode);
}
