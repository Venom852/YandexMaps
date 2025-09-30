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

    @Query("SELECT * FROM PointEntity WHERE id = :id")
    fun getPost(id: Long): PointEntity

    @Query("SELECT COUNT(*) == 0 FROM PointEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PointEntity")
    suspend fun count(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: PointEntity)
    @Query("UPDATE PointEntity Set title = :title, " +
            "city = :city, latitude = :latitude, " +
            "longitude = :longitude, detailedInformation = :detailedInformation WHERE id = :id")
    suspend fun changeContentById(id: Long, title: String, city: String, latitude: Double, longitude: Double, detailedInformation: String)

    suspend fun save(point: PointEntity) =
        if (point.id == 0L) insert(point) else changeContentById(point.id, point.title,
            point.city.toString(), point.latitude,
            point.longitude, point.detailedInformation.toString())

    @Query("DELETE FROM PointEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PointEntity")
    suspend fun removeAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(pointDraftEntity: PointDraftEntity)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertDraft(contentDraftEntity: PointDraftEntity)
//    suspend fun saveDraft(draft: String) = insertDraft(PointDraftEntity(contentDraft = draft))

    @Query("DELETE FROM PointDraftEntity")
    suspend fun removeDraft()

    @Query("SELECT * FROM PointDraftEntity")
    suspend fun getDraft(): PointDraftEntity
}