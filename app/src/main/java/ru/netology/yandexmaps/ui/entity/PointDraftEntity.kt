package ru.netology.yandexmaps.ui.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PointDraftEntity (
    @PrimaryKey
    val id: Long,
    val title: String?,
    val city: String?,
    val latitude: String?,
    val longitude: String?,
    val detailedInformation: String?,
)