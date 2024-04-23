package com.example.qup.app

import android.app.Application

//Creates single container instance to be used by application
class QueueApplicationContainer : Application() {
    lateinit var container: AppContainer
        private set
    override fun onCreate(){
        super.onCreate()
        container = AppDataContainer()
    }
}