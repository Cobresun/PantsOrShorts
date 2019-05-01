package com.cobresun.brun.pantsorshorts.repositories;

public interface UserDataRepository {

    float readUserThreshold();

    void writeUserThreshold(float threshold);

    long readLastTimeFetchedWeather();

    void writeLastTimeFetchedWeather(long time);

    int readLastFetchedTemp();

    void writeLastFetchedTemp(int temp);

    int readLastFetchedTempHigh();

    void writeLastFetchedTempHigh(int temp);

    int readLastFetchedTempLow();

    void writeLastFetchedTempLow(int temp);

    boolean isFirstTimeLaunching();

    boolean isNightMode();

    void writeNightMode(boolean nightMode);

    boolean isCelsius();

    void writeIsCelsius(boolean isCelsius);
}
