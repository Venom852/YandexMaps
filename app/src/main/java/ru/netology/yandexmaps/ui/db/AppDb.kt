package ru.netology.yandexmaps.ui.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netology.yandexmaps.ui.dao.PointDao
import ru.netology.yandexmaps.ui.entity.PointEntity
import ru.netology.yandexmaps.ui.entity.PointDraftEntity

@Database(entities = [PointEntity::class, PointDraftEntity::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract val pointDao: PointDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) = Room
            .databaseBuilder(context, AppDb::class.java, "app.db")
            .build()
    }
}