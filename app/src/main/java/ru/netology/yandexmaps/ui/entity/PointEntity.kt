package ru.netology.yandexmaps.ui.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.yandexmaps.ui.dto.PointDto
import kotlin.Long

@Entity
data class PointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val city: String?,
    val latitude: Double,
    val longitude: Double,
    val detailedInformation: String?,
    val photo: String?
) {
    fun toDto() = PointDto(
        id,
        title,
        city,
        latitude,
        longitude,
        detailedInformation,
        photo
    )

    companion object {
        fun fromDto(pointDto: PointDto) = PointEntity(
            pointDto.id,
            pointDto.title,
            pointDto.city,
            pointDto.latitude,
            pointDto.longitude,
            pointDto.detailedInformation,
            pointDto.photo
        )
    }
}

fun List<PointEntity>.toDto(): List<PointDto> = map(PointEntity::toDto)
fun List<PointDto>.toEntity(): List<PointEntity> = map(PointEntity::fromDto)