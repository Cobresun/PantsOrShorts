package com.cobresun.brun.pantsorshorts

interface Factory<T> {
    fun create(): T
}

class MainViewModelFactory(
    private val userDataRepository: UserDataRepository,
    private val weatherRepository: WeatherRepository
) : Factory<MainViewModel> {

    override fun create(): MainViewModel {
        return MainViewModel(userDataRepository, weatherRepository)
    }
}
