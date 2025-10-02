package ru.netology.yandexmaps.ui.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.yandexmaps.ui.entity.PointDraftEntity
import ru.netology.yandexmaps.ui.entity.PointEntity

@Dao
interface PointDao {
    @Query("SELECT * FROM PointEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: PointEntity)

    @Query("UPDATE PointEntity Set title = :title, " +
            "city = :city, latitude = :latitude, " +
            "longitude = :longitude, detailedInformation = :detailedInformation, photo = :photo WHERE id = :id")
    suspend fun changeContentById(id: Long, title: String, city: String, latitude: Double, longitude: Double, detailedInformation: String, photo: String)

    suspend fun save(point: PointEntity) =
        if (point.id == 0L) insert(point) else changeContentById(point.id, point.title,
            point.city.toString(), point.latitude,
            point.longitude, point.detailedInformation.toString(), point.photo.toString())

    @Query("DELETE FROM PointEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(pointDraftEntity: PointDraftEntity)

    @Query("SELECT COUNT(*) == 0 FROM PointDraftEntity")
    suspend fun isEmptyDraft(): Boolean


    @Query("DELETE FROM PointDraftEntity")
    suspend fun removeDraft()

    @Query("SELECT * FROM PointDraftEntity")
    suspend fun getDraft(): PointDraftEntity
}