package ru.netology.yandexmaps.ui.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.yandexmaps.ui.dao.PointDao
import ru.netology.yandexmaps.ui.entity.PointEntity
import ru.netology.yandexmaps.ui.entity.PointDraftEntity

@Database(entities = [PointEntity::class, PointDraftEntity::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract val postDao: PointDao
}