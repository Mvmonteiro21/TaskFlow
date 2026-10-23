package com.example.taskflow

import android.app.Application
import com.example.taskflow.di.AppContainer

class TasksApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
