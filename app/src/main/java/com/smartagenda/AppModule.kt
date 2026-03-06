package com.smartagenda

import android.content.Context
import com.smartagenda.data.PreferencesManager
import com.smartagenda.repository.SmartAgendaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun providePreferencesManager(@ApplicationContext ctx: Context) = PreferencesManager(ctx)

    @Provides @Singleton
    fun provideRepository(pm: PreferencesManager) = SmartAgendaRepository(pm)
}
