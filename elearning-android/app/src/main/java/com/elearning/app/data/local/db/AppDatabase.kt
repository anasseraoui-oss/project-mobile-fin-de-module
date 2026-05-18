package com.elearning.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.elearning.app.data.local.entity.FormationEntity
import com.elearning.app.data.local.entity.NotificationEntity
import com.elearning.app.data.local.entity.SeanceEntity

/**
 * Room Database — Single Source of Truth for offline-first data.
 * Version bump triggers a migration; use auto-migrations where possible.
 */
@Database(
    entities = [
        FormationEntity::class,
        SeanceEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun formationDao(): FormationDao
    abstract fun seanceDao(): SeanceDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DATABASE_NAME = "elearning_db"
    }
}
