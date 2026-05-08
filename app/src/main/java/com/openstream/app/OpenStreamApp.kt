package com.openstream.app

import android.app.Application
import com.openstream.app.core.plugins.ExtensionManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class OpenStreamApp : Application() {

    @Inject lateinit var extensionManager: ExtensionManager

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            extensionManager.loadAllInstalledExtensions()
        }
    }
}
