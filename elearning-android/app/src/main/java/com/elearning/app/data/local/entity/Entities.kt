package com.elearning.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// ─── Formation Entity ─────────────────────────────────────────────────────────

@Entity(tableName = "formations")
data class FormationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    val level: String,
    val language: String,
    val organisation: String,
    @ColumnInfo(name = "duration_hours") val durationHours: Int,
    val price: Double,
    val currency: String,
    val rating: Float,
    @ColumnInfo(name = "enrollment_count") val enrollmentCount: Int,
    @ColumnInfo(name = "course_count") val courseCount: Int,
    val tags: String,               // JSON-encoded List<String>
    @ColumnInfo(name = "is_enrolled") val isEnrolled: Boolean,
    @ColumnInfo(name = "progress_percent") val progressPercent: Int,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long = System.currentTimeMillis()
)

// ─── Seance Entity ────────────────────────────────────────────────────────────

@Entity(tableName = "seances")
data class SeanceEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "course_id") val courseId: String,
    @ColumnInfo(name = "formation_id") val formationId: String,
    val title: String,
    val description: String?,
    val type: String,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Int,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val status: String,
    @ColumnInfo(name = "video_key") val videoKey: String?,
    @ColumnInfo(name = "local_video_path") val localVideoPath: String?,   // offline
    @ColumnInfo(name = "meeting_link") val meetingLink: String?,
    @ColumnInfo(name = "scheduled_at") val scheduledAt: String?,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    @ColumnInfo(name = "progress_seconds") val progressSeconds: Int,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long = System.currentTimeMillis()
)

// ─── Notification Entity ──────────────────────────────────────────────────────

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    @ColumnInfo(name = "deep_link") val deepLink: String?,
    @ColumnInfo(name = "is_read") val isRead: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: String
)
