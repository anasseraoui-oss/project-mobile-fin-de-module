package com.elearning.app.`data`.local.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _formationDao: Lazy<FormationDao> = lazy {
    FormationDao_Impl(this)
  }

  private val _seanceDao: Lazy<SeanceDao> = lazy {
    SeanceDao_Impl(this)
  }

  private val _notificationDao: Lazy<NotificationDao> = lazy {
    NotificationDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "7c0174a181e699feed6f6c4fe5d3ae1a", "7748c40589517bb33c66e09cc24ec80c") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `formations` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `thumbnail_url` TEXT, `level` TEXT NOT NULL, `language` TEXT NOT NULL, `organisation` TEXT NOT NULL, `duration_hours` INTEGER NOT NULL, `price` REAL NOT NULL, `currency` TEXT NOT NULL, `rating` REAL NOT NULL, `enrollment_count` INTEGER NOT NULL, `course_count` INTEGER NOT NULL, `tags` TEXT NOT NULL, `is_enrolled` INTEGER NOT NULL, `progress_percent` INTEGER NOT NULL, `last_synced_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `seances` (`id` TEXT NOT NULL, `course_id` TEXT NOT NULL, `formation_id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `type` TEXT NOT NULL, `duration_seconds` INTEGER NOT NULL, `order_index` INTEGER NOT NULL, `status` TEXT NOT NULL, `video_key` TEXT, `local_video_path` TEXT, `meeting_link` TEXT, `scheduled_at` TEXT, `is_completed` INTEGER NOT NULL, `progress_seconds` INTEGER NOT NULL, `last_synced_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `type` TEXT NOT NULL, `deep_link` TEXT, `is_read` INTEGER NOT NULL, `created_at` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7c0174a181e699feed6f6c4fe5d3ae1a')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `formations`")
        connection.execSQL("DROP TABLE IF EXISTS `seances`")
        connection.execSQL("DROP TABLE IF EXISTS `notifications`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsFormations: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFormations.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("description", TableInfo.Column("description", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("thumbnail_url", TableInfo.Column("thumbnail_url", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("level", TableInfo.Column("level", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("language", TableInfo.Column("language", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("organisation", TableInfo.Column("organisation", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("duration_hours", TableInfo.Column("duration_hours", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("price", TableInfo.Column("price", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("currency", TableInfo.Column("currency", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("rating", TableInfo.Column("rating", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("enrollment_count", TableInfo.Column("enrollment_count", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("course_count", TableInfo.Column("course_count", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("tags", TableInfo.Column("tags", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("is_enrolled", TableInfo.Column("is_enrolled", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("progress_percent", TableInfo.Column("progress_percent", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFormations.put("last_synced_at", TableInfo.Column("last_synced_at", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFormations: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFormations: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFormations: TableInfo = TableInfo("formations", _columnsFormations,
            _foreignKeysFormations, _indicesFormations)
        val _existingFormations: TableInfo = read(connection, "formations")
        if (!_infoFormations.equals(_existingFormations)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |formations(com.elearning.app.data.local.entity.FormationEntity).
              | Expected:
              |""".trimMargin() + _infoFormations + """
              |
              | Found:
              |""".trimMargin() + _existingFormations)
        }
        val _columnsSeances: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSeances.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("course_id", TableInfo.Column("course_id", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("formation_id", TableInfo.Column("formation_id", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("description", TableInfo.Column("description", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("duration_seconds", TableInfo.Column("duration_seconds", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("order_index", TableInfo.Column("order_index", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("video_key", TableInfo.Column("video_key", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("local_video_path", TableInfo.Column("local_video_path", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("meeting_link", TableInfo.Column("meeting_link", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("scheduled_at", TableInfo.Column("scheduled_at", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("is_completed", TableInfo.Column("is_completed", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("progress_seconds", TableInfo.Column("progress_seconds", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSeances.put("last_synced_at", TableInfo.Column("last_synced_at", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSeances: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSeances: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSeances: TableInfo = TableInfo("seances", _columnsSeances, _foreignKeysSeances,
            _indicesSeances)
        val _existingSeances: TableInfo = read(connection, "seances")
        if (!_infoSeances.equals(_existingSeances)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |seances(com.elearning.app.data.local.entity.SeanceEntity).
              | Expected:
              |""".trimMargin() + _infoSeances + """
              |
              | Found:
              |""".trimMargin() + _existingSeances)
        }
        val _columnsNotifications: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsNotifications.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("body", TableInfo.Column("body", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("deep_link", TableInfo.Column("deep_link", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("is_read", TableInfo.Column("is_read", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("created_at", TableInfo.Column("created_at", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysNotifications: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesNotifications: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoNotifications: TableInfo = TableInfo("notifications", _columnsNotifications,
            _foreignKeysNotifications, _indicesNotifications)
        val _existingNotifications: TableInfo = read(connection, "notifications")
        if (!_infoNotifications.equals(_existingNotifications)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |notifications(com.elearning.app.data.local.entity.NotificationEntity).
              | Expected:
              |""".trimMargin() + _infoNotifications + """
              |
              | Found:
              |""".trimMargin() + _existingNotifications)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "formations", "seances",
        "notifications")
  }

  public override fun clearAllTables() {
    super.performClear(false, "formations", "seances", "notifications")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(FormationDao::class, FormationDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SeanceDao::class, SeanceDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(NotificationDao::class, NotificationDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun formationDao(): FormationDao = _formationDao.value

  public override fun seanceDao(): SeanceDao = _seanceDao.value

  public override fun notificationDao(): NotificationDao = _notificationDao.value
}
