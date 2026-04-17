package com.example.luontopeli.data.di

import android.content.Context
import com.example.luontopeli.data.local.AppDatabase
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.dao.WalkSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideNatureSpotDao(database: AppDatabase): NatureSpotDao {
        return database.natureSpotDao()
    }

    @Provides
    fun provideWalkSessionDao(database: AppDatabase): WalkSessionDao {
        return database.walkSessionDao()
    }
}
