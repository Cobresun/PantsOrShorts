package com.cobresun.brun.pantsorshorts

import android.app.Application

class MyApplication: Application() {
    val appContainer by lazy {
        AppContainer(applicationContext)
    }
}
