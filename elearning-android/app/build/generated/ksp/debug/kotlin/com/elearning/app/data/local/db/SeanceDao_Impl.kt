package com.elearning.app.`data`.local.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.elearning.app.`data`.local.entity.SeanceEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SeanceDao_Impl(
  __db: RoomDatabase,
) : SeanceDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfSeanceEntity: EntityUpsertAdapter<SeanceEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfSeanceEntity = EntityUpsertAdapter<SeanceEntity>(object :
        EntityInsertAdapter<SeanceEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `seances` (`id`,`course_id`,`formation_id`,`title`,`description`,`type`,`duration_seconds`,`order_index`,`status`,`video_key`,`local_video_path`,`meeting_link`,`scheduled_at`,`is_completed`,`progress_seconds`,`last_synced_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SeanceEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.courseId)
        statement.bindText(3, entity.formationId)
        statement.bindText(4, entity.title)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpDescription)
        }
        statement.bindText(6, entity.type)
        statement.bindLong(7, entity.durationSeconds.toLong())
        statement.bindLong(8, entity.orderIndex.toLong())
        statement.bindText(9, entity.status)
        val _tmpVideoKey: String? = entity.videoKey
        if (_tmpVideoKey == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpVideoKey)
        }
        val _tmpLocalVideoPath: String? = entity.localVideoPath
        if (_tmpLocalVideoPath == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpLocalVideoPath)
        }
        val _tmpMeetingLink: String? = entity.meetingLink
        if (_tmpMeetingLink == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpMeetingLink)
        }
        val _tmpScheduledAt: String? = entity.scheduledAt
        if (_tmpScheduledAt == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmpScheduledAt)
        }
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(14, _tmp.toLong())
        statement.bindLong(15, entity.progressSeconds.toLong())
        statement.bindLong(16, entity.lastSyncedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<SeanceEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `seances` SET `id` = ?,`course_id` = ?,`formation_id` = ?,`title` = ?,`description` = ?,`type` = ?,`duration_seconds` = ?,`order_index` = ?,`status` = ?,`video_key` = ?,`local_video_path` = ?,`meeting_link` = ?,`scheduled_at` = ?,`is_completed` = ?,`progress_seconds` = ?,`last_synced_at` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SeanceEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.courseId)
        statement.bindText(3, entity.formationId)
        statement.bindText(4, entity.title)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpDescription)
        }
        statement.bindText(6, entity.type)
        statement.bindLong(7, entity.durationSeconds.toLong())
        statement.bindLong(8, entity.orderIndex.toLong())
        statement.bindText(9, entity.status)
        val _tmpVideoKey: String? = entity.videoKey
        if (_tmpVideoKey == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpVideoKey)
        }
        val _tmpLocalVideoPath: String? = entity.localVideoPath
        if (_tmpLocalVideoPath == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpLocalVideoPath)
        }
        val _tmpMeetingLink: String? = entity.meetingLink
        if (_tmpMeetingLink == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpMeetingLink)
        }
        val _tmpScheduledAt: String? = entity.scheduledAt
        if (_tmpScheduledAt == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmpScheduledAt)
        }
        val _tmp: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(14, _tmp.toLong())
        statement.bindLong(15, entity.progressSeconds.toLong())
        statement.bindLong(16, entity.lastSyncedAt)
        statement.bindText(17, entity.id)
      }
    })
  }

  public override suspend fun upsertAll(seances: List<SeanceEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __upsertAdapterOfSeanceEntity.upsert(_connection, seances)
  }

  public override suspend fun upsert(seance: SeanceEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __upsertAdapterOfSeanceEntity.upsert(_connection, seance)
  }

  public override fun observeByCourse(courseId: String): Flow<List<SeanceEntity>> {
    val _sql: String = "SELECT * FROM seances WHERE course_id = ? ORDER BY order_index ASC"
    return createFlow(__db, false, arrayOf("seances")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, courseId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCourseId: Int = getColumnIndexOrThrow(_stmt, "course_id")
        val _columnIndexOfFormationId: Int = getColumnIndexOrThrow(_stmt, "formation_id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "duration_seconds")
        val _columnIndexOfOrderIndex: Int = getColumnIndexOrThrow(_stmt, "order_index")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVideoKey: Int = getColumnIndexOrThrow(_stmt, "video_key")
        val _columnIndexOfLocalVideoPath: Int = getColumnIndexOrThrow(_stmt, "local_video_path")
        val _columnIndexOfMeetingLink: Int = getColumnIndexOrThrow(_stmt, "meeting_link")
        val _columnIndexOfScheduledAt: Int = getColumnIndexOrThrow(_stmt, "scheduled_at")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "is_completed")
        val _columnIndexOfProgressSeconds: Int = getColumnIndexOrThrow(_stmt, "progress_seconds")
        val _columnIndexOfLastSyncedAt: Int = getColumnIndexOrThrow(_stmt, "last_synced_at")
        val _result: MutableList<SeanceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SeanceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpCourseId: String
          _tmpCourseId = _stmt.getText(_columnIndexOfCourseId)
          val _tmpFormationId: String
          _tmpFormationId = _stmt.getText(_columnIndexOfFormationId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpDurationSeconds: Int
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds).toInt()
          val _tmpOrderIndex: Int
          _tmpOrderIndex = _stmt.getLong(_columnIndexOfOrderIndex).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpVideoKey: String?
          if (_stmt.isNull(_columnIndexOfVideoKey)) {
            _tmpVideoKey = null
          } else {
            _tmpVideoKey = _stmt.getText(_columnIndexOfVideoKey)
          }
          val _tmpLocalVideoPath: String?
          if (_stmt.isNull(_columnIndexOfLocalVideoPath)) {
            _tmpLocalVideoPath = null
          } else {
            _tmpLocalVideoPath = _stmt.getText(_columnIndexOfLocalVideoPath)
          }
          val _tmpMeetingLink: String?
          if (_stmt.isNull(_columnIndexOfMeetingLink)) {
            _tmpMeetingLink = null
          } else {
            _tmpMeetingLink = _stmt.getText(_columnIndexOfMeetingLink)
          }
          val _tmpScheduledAt: String?
          if (_stmt.isNull(_columnIndexOfScheduledAt)) {
            _tmpScheduledAt = null
          } else {
            _tmpScheduledAt = _stmt.getText(_columnIndexOfScheduledAt)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpProgressSeconds: Int
          _tmpProgressSeconds = _stmt.getLong(_columnIndexOfProgressSeconds).toInt()
          val _tmpLastSyncedAt: Long
          _tmpLastSyncedAt = _stmt.getLong(_columnIndexOfLastSyncedAt)
          _item =
              SeanceEntity(_tmpId,_tmpCourseId,_tmpFormationId,_tmpTitle,_tmpDescription,_tmpType,_tmpDurationSeconds,_tmpOrderIndex,_tmpStatus,_tmpVideoKey,_tmpLocalVideoPath,_tmpMeetingLink,_tmpScheduledAt,_tmpIsCompleted,_tmpProgressSeconds,_tmpLastSyncedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findById(id: String): SeanceEntity? {
    val _sql: String = "SELECT * FROM seances WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCourseId: Int = getColumnIndexOrThrow(_stmt, "course_id")
        val _columnIndexOfFormationId: Int = getColumnIndexOrThrow(_stmt, "formation_id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "duration_seconds")
        val _columnIndexOfOrderIndex: Int = getColumnIndexOrThrow(_stmt, "order_index")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfVideoKey: Int = getColumnIndexOrThrow(_stmt, "video_key")
        val _columnIndexOfLocalVideoPath: Int = getColumnIndexOrThrow(_stmt, "local_video_path")
        val _columnIndexOfMeetingLink: Int = getColumnIndexOrThrow(_stmt, "meeting_link")
        val _columnIndexOfScheduledAt: Int = getColumnIndexOrThrow(_stmt, "scheduled_at")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "is_completed")
        val _columnIndexOfProgressSeconds: Int = getColumnIndexOrThrow(_stmt, "progress_seconds")
        val _columnIndexOfLastSyncedAt: Int = getColumnIndexOrThrow(_stmt, "last_synced_at")
        val _result: SeanceEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpCourseId: String
          _tmpCourseId = _stmt.getText(_columnIndexOfCourseId)
          val _tmpFormationId: String
          _tmpFormationId = _stmt.getText(_columnIndexOfFormationId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpDurationSeconds: Int
          _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds).toInt()
          val _tmpOrderIndex: Int
          _tmpOrderIndex = _stmt.getLong(_columnIndexOfOrderIndex).toInt()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpVideoKey: String?
          if (_stmt.isNull(_columnIndexOfVideoKey)) {
            _tmpVideoKey = null
          } else {
            _tmpVideoKey = _stmt.getText(_columnIndexOfVideoKey)
          }
          val _tmpLocalVideoPath: String?
          if (_stmt.isNull(_columnIndexOfLocalVideoPath)) {
            _tmpLocalVideoPath = null
          } else {
            _tmpLocalVideoPath = _stmt.getText(_columnIndexOfLocalVideoPath)
          }
          val _tmpMeetingLink: String?
          if (_stmt.isNull(_columnIndexOfMeetingLink)) {
            _tmpMeetingLink = null
          } else {
            _tmpMeetingLink = _stmt.getText(_columnIndexOfMeetingLink)
          }
          val _tmpScheduledAt: String?
          if (_stmt.isNull(_columnIndexOfScheduledAt)) {
            _tmpScheduledAt = null
          } else {
            _tmpScheduledAt = _stmt.getText(_columnIndexOfScheduledAt)
          }
          val _tmpIsCompleted: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp != 0
          val _tmpProgressSeconds: Int
          _tmpProgressSeconds = _stmt.getLong(_columnIndexOfProgressSeconds).toInt()
          val _tmpLastSyncedAt: Long
          _tmpLastSyncedAt = _stmt.getLong(_columnIndexOfLastSyncedAt)
          _result =
              SeanceEntity(_tmpId,_tmpCourseId,_tmpFormationId,_tmpTitle,_tmpDescription,_tmpType,_tmpDurationSeconds,_tmpOrderIndex,_tmpStatus,_tmpVideoKey,_tmpLocalVideoPath,_tmpMeetingLink,_tmpScheduledAt,_tmpIsCompleted,_tmpProgressSeconds,_tmpLastSyncedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateProgress(
    id: String,
    progress: Int,
    completed: Boolean,
  ) {
    val _sql: String = "UPDATE seances SET progress_seconds = ?, is_completed = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, progress.toLong())
        _argIndex = 2
        val _tmp: Int = if (completed) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 3
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteForCourse(courseId: String) {
    val _sql: String = "DELETE FROM seances WHERE course_id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, courseId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
