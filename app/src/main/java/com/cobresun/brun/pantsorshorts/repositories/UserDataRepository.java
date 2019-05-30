package com.cobresun.brun.pantsorshorts.repositories;

public interface UserDataRepository {

    int readUserThreshold();

    void writeUserThreshold(int threshold);

    long readLastTimeFetchedWeather();

    void writeLastTimeFetchedWeather(long time);

    int readLastFetchedTemp();

    void writeLastFetchedTemp(int temp);

    int readLastFetchedTempHigh();

    void writeLastFetchedTempHigh(int temp);

    int readLastFetchedTempLow();

    void writeLastFetchedTempLow(int temp);

    int[] readLastFetchedHourlyTemps();

    void writeLastFetchedHourlyTemps(int[] temps);

    boolean isFirstTimeLaunching();

    boolean isNightMode();

    void writeNightMode(boolean nightMode);

    boolean isCelsius();

    void writeIsCelsius(boolean isCelsius);
}
