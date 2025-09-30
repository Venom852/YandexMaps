package ru.netology.yandexmaps.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.netology.yandexmaps.databinding.CardPointBinding
import ru.netology.yandexmaps.ui.dto.PointDto

class PointAdapter (
    private val onInteractionListener: OnInteractionListener
) : ListAdapter<PointDto, PointViewHolder>(PointDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CardPointBinding.inflate(layoutInflater, parent, false)
        return PointViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}