package com.elearning.app.`data`.local.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.elearning.app.`data`.local.entity.NotificationEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
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
public class NotificationDao_Impl(
  __db: RoomDatabase,
) : NotificationDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfNotificationEntity: EntityUpsertAdapter<NotificationEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfNotificationEntity = EntityUpsertAdapter<NotificationEntity>(object :
        EntityInsertAdapter<NotificationEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `notifications` (`id`,`title`,`body`,`type`,`deep_link`,`is_read`,`created_at`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: NotificationEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.body)
        statement.bindText(4, entity.type)
        val _tmpDeepLink: String? = entity.deepLink
        if (_tmpDeepLink == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpDeepLink)
        }
        val _tmp: Int = if (entity.isRead) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.createdAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<NotificationEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `notifications` SET `id` = ?,`title` = ?,`body` = ?,`type` = ?,`deep_link` = ?,`is_read` = ?,`created_at` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: NotificationEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.body)
        statement.bindText(4, entity.type)
        val _tmpDeepLink: String? = entity.deepLink
        if (_tmpDeepLink == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpDeepLink)
        }
        val _tmp: Int = if (entity.isRead) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.createdAt)
        statement.bindText(8, entity.id)
      }
    })
  }

  public override suspend fun upsert(notification: NotificationEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfNotificationEntity.upsert(_connection, notification)
  }

  public override fun observeAll(): Flow<List<NotificationEntity>> {
    val _sql: String = "SELECT * FROM notifications ORDER BY created_at DESC"
    return createFlow(__db, false, arrayOf("notifications")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfDeepLink: Int = getColumnIndexOrThrow(_stmt, "deep_link")
        val _columnIndexOfIsRead: Int = getColumnIndexOrThrow(_stmt, "is_read")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _result: MutableList<NotificationEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: NotificationEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: String
          _tmpBody = _stmt.getText(_columnIndexOfBody)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpDeepLink: String?
          if (_stmt.isNull(_columnIndexOfDeepLink)) {
            _tmpDeepLink = null
          } else {
            _tmpDeepLink = _stmt.getText(_columnIndexOfDeepLink)
          }
          val _tmpIsRead: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsRead).toInt()
          _tmpIsRead = _tmp != 0
          val _tmpCreatedAt: String
          _tmpCreatedAt = _stmt.getText(_columnIndexOfCreatedAt)
          _item =
              NotificationEntity(_tmpId,_tmpTitle,_tmpBody,_tmpType,_tmpDeepLink,_tmpIsRead,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeUnreadCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM notifications WHERE is_read = 0"
    return createFlow(__db, false, arrayOf("notifications")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsRead(id: String) {
    val _sql: String = "UPDATE notifications SET is_read = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAllAsRead() {
    val _sql: String = "UPDATE notifications SET is_read = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM notifications WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
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
