package ru.netology.yandexmaps.ui.adapter

import android.os.Bundle
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.netology.yandexmaps.databinding.CardPointBinding
import com.google.gson.Gson
import ru.netology.yandexmaps.R
import ru.netology.yandexmaps.ui.dto.PointDto
import ru.netology.yandexmaps.ui.extensions.setAllOnClickListener
import ru.netology.yandexmaps.ui.fragment.MapsFragment.Companion.pointDtoData
import ru.netology.yandexmaps.ui.fragment.PointFragment.Companion.textPoint

class PointViewHolder(
    private val binding: CardPointBinding,
    private val onInteractionListener: OnInteractionListener,
    private val gson: Gson = Gson()
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(pointDto: PointDto) {
        with(binding) {
            title.text = pointDto.title
            city.text = pointDto.city

            location.setOnClickListener {
                findNavController(it).navigate(
                    R.id.action_listPointsFragment2_to_mapsFragment,
                    Bundle().apply {
                        pointDtoData = gson.toJson(pointDto)
                    })
            }

            groupPoint.setAllOnClickListener {
                findNavController(it).navigate(
                    R.id.action_listPointsFragment2_to_pointFragment2,
                    Bundle().apply {
                        textPoint = gson.toJson(pointDto)
                    })
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_point)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.detailed -> {
                                findNavController(it).navigate(
                                    R.id.action_listPointsFragment2_to_pointFragment2,
                                    Bundle().apply {
                                        textPoint = gson.toJson(pointDto)
                                    })
                                true
                            }
                            R.id.remove -> {
                                onInteractionListener.onRemove(pointDto)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(pointDto)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}