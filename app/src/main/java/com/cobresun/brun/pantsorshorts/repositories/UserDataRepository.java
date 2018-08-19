package com.cobresun.brun.pantsorshorts.repositories;

public interface UserDataRepository {

    float readUserThreshold();

    void writeUserThreshold(float threshold);
}
