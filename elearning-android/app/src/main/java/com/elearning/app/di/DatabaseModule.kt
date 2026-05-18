package com.elearning.app.di

import android.content.Context
import androidx.room.Room
import com.elearning.app.data.local.db.AppDatabase
import com.elearning.app.data.local.db.FormationDao
import com.elearning.app.data.local.db.NotificationDao
import com.elearning.app.data.local.db.SeanceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule — provides the Room database instance and its DAOs.
 * DataStore is initialized via [TokenManager] which is @Singleton and auto-injected.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)   // In production: use proper migrations
            .build()

    @Provides
    @Singleton
    fun provideFormationDao(db: AppDatabase): FormationDao = db.formationDao()

    @Provides
    @Singleton
    fun provideSeanceDao(db: AppDatabase): SeanceDao = db.seanceDao()

    @Provides
    @Singleton
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()
}
