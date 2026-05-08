package com.openstream.app.di

import com.openstream.app.core.plugins.AggregatedSearch
import com.openstream.app.core.plugins.ExtensionManager
import com.openstream.app.core.plugins.HomePageAggregator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PluginModule {

    @Provides @Singleton
    fun provideAggregatedSearch(em: ExtensionManager): AggregatedSearch =
        AggregatedSearch(em)

    @Provides @Singleton
    fun provideHomePageAggregator(em: ExtensionManager): HomePageAggregator =
        HomePageAggregator(em)
}
