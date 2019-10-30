package com.cobresun.brun.pantsorshorts;

public interface UserDataRepository {

    boolean hasUserUpdated();

    void writeHasUserUpdated(boolean userUpdated);

    void clearUserThreshold();

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
