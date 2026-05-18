package com.elearning.app.`data`.local.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.elearning.app.`data`.local.entity.FormationEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Float
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
public class FormationDao_Impl(
  __db: RoomDatabase,
) : FormationDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfFormationEntity: EntityUpsertAdapter<FormationEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfFormationEntity = EntityUpsertAdapter<FormationEntity>(object :
        EntityInsertAdapter<FormationEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `formations` (`id`,`title`,`description`,`thumbnail_url`,`level`,`language`,`organisation`,`duration_hours`,`price`,`currency`,`rating`,`enrollment_count`,`course_count`,`tags`,`is_enrolled`,`progress_percent`,`last_synced_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FormationEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        val _tmpThumbnailUrl: String? = entity.thumbnailUrl
        if (_tmpThumbnailUrl == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpThumbnailUrl)
        }
        statement.bindText(5, entity.level)
        statement.bindText(6, entity.language)
        statement.bindText(7, entity.organisation)
        statement.bindLong(8, entity.durationHours.toLong())
        statement.bindDouble(9, entity.price)
        statement.bindText(10, entity.currency)
        statement.bindDouble(11, entity.rating.toDouble())
        statement.bindLong(12, entity.enrollmentCount.toLong())
        statement.bindLong(13, entity.courseCount.toLong())
        statement.bindText(14, entity.tags)
        val _tmp: Int = if (entity.isEnrolled) 1 else 0
        statement.bindLong(15, _tmp.toLong())
        statement.bindLong(16, entity.progressPercent.toLong())
        statement.bindLong(17, entity.lastSyncedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<FormationEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `formations` SET `id` = ?,`title` = ?,`description` = ?,`thumbnail_url` = ?,`level` = ?,`language` = ?,`organisation` = ?,`duration_hours` = ?,`price` = ?,`currency` = ?,`rating` = ?,`enrollment_count` = ?,`course_count` = ?,`tags` = ?,`is_enrolled` = ?,`progress_percent` = ?,`last_synced_at` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FormationEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        val _tmpThumbnailUrl: String? = entity.thumbnailUrl
        if (_tmpThumbnailUrl == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpThumbnailUrl)
        }
        statement.bindText(5, entity.level)
        statement.bindText(6, entity.language)
        statement.bindText(7, entity.organisation)
        statement.bindLong(8, entity.durationHours.toLong())
        statement.bindDouble(9, entity.price)
        statement.bindText(10, entity.currency)
        statement.bindDouble(11, entity.rating.toDouble())
        statement.bindLong(12, entity.enrollmentCount.toLong())
        statement.bindLong(13, entity.courseCount.toLong())
        statement.bindText(14, entity.tags)
        val _tmp: Int = if (entity.isEnrolled) 1 else 0
        statement.bindLong(15, _tmp.toLong())
        statement.bindLong(16, entity.progressPercent.toLong())
        statement.bindLong(17, entity.lastSyncedAt)
        statement.bindText(18, entity.id)
      }
    })
  }

  public override suspend fun upsertAll(formations: List<FormationEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfFormationEntity.upsert(_connection, formations)
  }

  public override suspend fun upsert(formation: FormationEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __upsertAdapterOfFormationEntity.upsert(_connection, formation)
  }

  public override fun observeAll(): Flow<List<FormationEntity>> {
    val _sql: String = "SELECT * FROM formations ORDER BY title ASC"
    return createFlow(__db, false, arrayOf("formations")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnail_url")
        val _columnIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _columnIndexOfOrganisation: Int = getColumnIndexOrThrow(_stmt, "organisation")
        val _columnIndexOfDurationHours: Int = getColumnIndexOrThrow(_stmt, "duration_hours")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfRating: Int = getColumnIndexOrThrow(_stmt, "rating")
        val _columnIndexOfEnrollmentCount: Int = getColumnIndexOrThrow(_stmt, "enrollment_count")
        val _columnIndexOfCourseCount: Int = getColumnIndexOrThrow(_stmt, "course_count")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfIsEnrolled: Int = getColumnIndexOrThrow(_stmt, "is_enrolled")
        val _columnIndexOfProgressPercent: Int = getColumnIndexOrThrow(_stmt, "progress_percent")
        val _columnIndexOfLastSyncedAt: Int = getColumnIndexOrThrow(_stmt, "last_synced_at")
        val _result: MutableList<FormationEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FormationEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpLevel: String
          _tmpLevel = _stmt.getText(_columnIndexOfLevel)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          val _tmpOrganisation: String
          _tmpOrganisation = _stmt.getText(_columnIndexOfOrganisation)
          val _tmpDurationHours: Int
          _tmpDurationHours = _stmt.getLong(_columnIndexOfDurationHours).toInt()
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpRating: Float
          _tmpRating = _stmt.getDouble(_columnIndexOfRating).toFloat()
          val _tmpEnrollmentCount: Int
          _tmpEnrollmentCount = _stmt.getLong(_columnIndexOfEnrollmentCount).toInt()
          val _tmpCourseCount: Int
          _tmpCourseCount = _stmt.getLong(_columnIndexOfCourseCount).toInt()
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpIsEnrolled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEnrolled).toInt()
          _tmpIsEnrolled = _tmp != 0
          val _tmpProgressPercent: Int
          _tmpProgressPercent = _stmt.getLong(_columnIndexOfProgressPercent).toInt()
          val _tmpLastSyncedAt: Long
          _tmpLastSyncedAt = _stmt.getLong(_columnIndexOfLastSyncedAt)
          _item =
              FormationEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpThumbnailUrl,_tmpLevel,_tmpLanguage,_tmpOrganisation,_tmpDurationHours,_tmpPrice,_tmpCurrency,_tmpRating,_tmpEnrollmentCount,_tmpCourseCount,_tmpTags,_tmpIsEnrolled,_tmpProgressPercent,_tmpLastSyncedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findById(id: String): FormationEntity? {
    val _sql: String = "SELECT * FROM formations WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnail_url")
        val _columnIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _columnIndexOfOrganisation: Int = getColumnIndexOrThrow(_stmt, "organisation")
        val _columnIndexOfDurationHours: Int = getColumnIndexOrThrow(_stmt, "duration_hours")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfRating: Int = getColumnIndexOrThrow(_stmt, "rating")
        val _columnIndexOfEnrollmentCount: Int = getColumnIndexOrThrow(_stmt, "enrollment_count")
        val _columnIndexOfCourseCount: Int = getColumnIndexOrThrow(_stmt, "course_count")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfIsEnrolled: Int = getColumnIndexOrThrow(_stmt, "is_enrolled")
        val _columnIndexOfProgressPercent: Int = getColumnIndexOrThrow(_stmt, "progress_percent")
        val _columnIndexOfLastSyncedAt: Int = getColumnIndexOrThrow(_stmt, "last_synced_at")
        val _result: FormationEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpLevel: String
          _tmpLevel = _stmt.getText(_columnIndexOfLevel)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          val _tmpOrganisation: String
          _tmpOrganisation = _stmt.getText(_columnIndexOfOrganisation)
          val _tmpDurationHours: Int
          _tmpDurationHours = _stmt.getLong(_columnIndexOfDurationHours).toInt()
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpRating: Float
          _tmpRating = _stmt.getDouble(_columnIndexOfRating).toFloat()
          val _tmpEnrollmentCount: Int
          _tmpEnrollmentCount = _stmt.getLong(_columnIndexOfEnrollmentCount).toInt()
          val _tmpCourseCount: Int
          _tmpCourseCount = _stmt.getLong(_columnIndexOfCourseCount).toInt()
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpIsEnrolled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEnrolled).toInt()
          _tmpIsEnrolled = _tmp != 0
          val _tmpProgressPercent: Int
          _tmpProgressPercent = _stmt.getLong(_columnIndexOfProgressPercent).toInt()
          val _tmpLastSyncedAt: Long
          _tmpLastSyncedAt = _stmt.getLong(_columnIndexOfLastSyncedAt)
          _result =
              FormationEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpThumbnailUrl,_tmpLevel,_tmpLanguage,_tmpOrganisation,_tmpDurationHours,_tmpPrice,_tmpCurrency,_tmpRating,_tmpEnrollmentCount,_tmpCourseCount,_tmpTags,_tmpIsEnrolled,_tmpProgressPercent,_tmpLastSyncedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun searchPaged(
    query: String,
    level: String,
    language: String,
    pageSize: Int,
    offset: Int,
  ): List<FormationEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM formations 
        |        WHERE (? = '' OR title LIKE '%' || ? || '%' OR description LIKE '%' || ? || '%')
        |        AND (? = '' OR level = ?)
        |        AND (? = '' OR language = ?)
        |        ORDER BY title ASC
        |        LIMIT ? OFFSET ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        _argIndex = 2
        _stmt.bindText(_argIndex, query)
        _argIndex = 3
        _stmt.bindText(_argIndex, query)
        _argIndex = 4
        _stmt.bindText(_argIndex, level)
        _argIndex = 5
        _stmt.bindText(_argIndex, level)
        _argIndex = 6
        _stmt.bindText(_argIndex, language)
        _argIndex = 7
        _stmt.bindText(_argIndex, language)
        _argIndex = 8
        _stmt.bindLong(_argIndex, pageSize.toLong())
        _argIndex = 9
        _stmt.bindLong(_argIndex, offset.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnail_url")
        val _columnIndexOfLevel: Int = getColumnIndexOrThrow(_stmt, "level")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _columnIndexOfOrganisation: Int = getColumnIndexOrThrow(_stmt, "organisation")
        val _columnIndexOfDurationHours: Int = getColumnIndexOrThrow(_stmt, "duration_hours")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfRating: Int = getColumnIndexOrThrow(_stmt, "rating")
        val _columnIndexOfEnrollmentCount: Int = getColumnIndexOrThrow(_stmt, "enrollment_count")
        val _columnIndexOfCourseCount: Int = getColumnIndexOrThrow(_stmt, "course_count")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfIsEnrolled: Int = getColumnIndexOrThrow(_stmt, "is_enrolled")
        val _columnIndexOfProgressPercent: Int = getColumnIndexOrThrow(_stmt, "progress_percent")
        val _columnIndexOfLastSyncedAt: Int = getColumnIndexOrThrow(_stmt, "last_synced_at")
        val _result: MutableList<FormationEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FormationEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpLevel: String
          _tmpLevel = _stmt.getText(_columnIndexOfLevel)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          val _tmpOrganisation: String
          _tmpOrganisation = _stmt.getText(_columnIndexOfOrganisation)
          val _tmpDurationHours: Int
          _tmpDurationHours = _stmt.getLong(_columnIndexOfDurationHours).toInt()
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpRating: Float
          _tmpRating = _stmt.getDouble(_columnIndexOfRating).toFloat()
          val _tmpEnrollmentCount: Int
          _tmpEnrollmentCount = _stmt.getLong(_columnIndexOfEnrollmentCount).toInt()
          val _tmpCourseCount: Int
          _tmpCourseCount = _stmt.getLong(_columnIndexOfCourseCount).toInt()
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpIsEnrolled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEnrolled).toInt()
          _tmpIsEnrolled = _tmp != 0
          val _tmpProgressPercent: Int
          _tmpProgressPercent = _stmt.getLong(_columnIndexOfProgressPercent).toInt()
          val _tmpLastSyncedAt: Long
          _tmpLastSyncedAt = _stmt.getLong(_columnIndexOfLastSyncedAt)
          _item =
              FormationEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpThumbnailUrl,_tmpLevel,_tmpLanguage,_tmpOrganisation,_tmpDurationHours,_tmpPrice,_tmpCurrency,_tmpRating,_tmpEnrollmentCount,_tmpCourseCount,_tmpTags,_tmpIsEnrolled,_tmpProgressPercent,_tmpLastSyncedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM formations"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateEnrollment(id: String, progress: Int) {
    val _sql: String = "UPDATE formations SET is_enrolled = 1, progress_percent = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, progress.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
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
