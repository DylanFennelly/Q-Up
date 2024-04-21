package com.example.qup.app

import android.app.Application
import com.example.qup.data.RequestsRepository

//Creates single container instance to be used by application
class QueueApplicationContainer : Application() {
    lateinit var container: AppContainer
    lateinit var requestsRepository: RequestsRepository
        private set
    override fun onCreate(){
        super.onCreate()
        container = AppDataContainer()
        requestsRepository = RequestsRepository(applicationContext)
    }
}