package ru.netology.yandexmaps.ui.dto

data class PointDto(
    val id: Long,
    val title: String,
    val city: String?,
    val latitude: Double,
    val longitude: Double,
    val detailedInformation: String?,
    val photo: String?
)