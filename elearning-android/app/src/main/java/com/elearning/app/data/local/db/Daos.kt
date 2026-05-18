package com.elearning.app.data.local.db

import androidx.room.*
import com.elearning.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

// ─── Formation DAO ────────────────────────────────────────────────────────────

@Dao
interface FormationDao {

    @Query("SELECT * FROM formations ORDER BY title ASC")
    fun observeAll(): Flow<List<FormationEntity>>

    @Query("SELECT * FROM formations WHERE id = :id")
    suspend fun findById(id: String): FormationEntity?

    @Query("""
        SELECT * FROM formations 
        WHERE (:query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND (:level = '' OR level = :level)
        AND (:language = '' OR language = :language)
        ORDER BY title ASC
        LIMIT :pageSize OFFSET :offset
    """)
    suspend fun searchPaged(
        query: String,
        level: String,
        language: String,
        pageSize: Int,
        offset: Int
    ): List<FormationEntity>

    @Upsert
    suspend fun upsertAll(formations: List<FormationEntity>)

    @Upsert
    suspend fun upsert(formation: FormationEntity)

    @Query("DELETE FROM formations")
    suspend fun deleteAll()

    @Query("UPDATE formations SET is_enrolled = 1, progress_percent = :progress WHERE id = :id")
    suspend fun updateEnrollment(id: String, progress: Int = 0)
}

// ─── Seance DAO ───────────────────────────────────────────────────────────────

@Dao
interface SeanceDao {

    @Query("SELECT * FROM seances WHERE course_id = :courseId ORDER BY order_index ASC")
    fun observeByCourse(courseId: String): Flow<List<SeanceEntity>>

    @Query("SELECT * FROM seances WHERE id = :id")
    suspend fun findById(id: String): SeanceEntity?

    @Upsert
    suspend fun upsertAll(seances: List<SeanceEntity>)

    @Upsert
    suspend fun upsert(seance: SeanceEntity)

    @Query("UPDATE seances SET progress_seconds = :progress, is_completed = :completed WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, completed: Boolean)

    @Query("DELETE FROM seances WHERE course_id = :courseId")
    suspend fun deleteForCourse(courseId: String)
}

// ─── Notification DAO ─────────────────────────────────────────────────────────

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE is_read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Upsert
    suspend fun upsert(notification: NotificationEntity)

    @Query("UPDATE notifications SET is_read = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET is_read = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun delete(id: String)
}
