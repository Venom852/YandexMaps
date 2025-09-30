package ru.netology.yandexmaps.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.netology.yandexmaps.ui.dto.PointDto

class PointDiffCallback : DiffUtil.ItemCallback<PointDto>() {
    override fun areItemsTheSame(oldItem: PointDto, newItem: PointDto): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PointDto, newItem: PointDto): Boolean = oldItem == newItem
}