package ru.netology.yandexmaps.ui.adapter

import ru.netology.yandexmaps.ui.dto.PointDto

interface OnInteractionListener {
    fun onEdit(pointDto: PointDto)
    fun onRemove(pointDto: PointDto)
}