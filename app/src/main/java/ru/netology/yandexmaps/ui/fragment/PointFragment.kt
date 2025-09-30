package ru.netology.yandexmaps.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.yandexmaps.R
import ru.netology.yandexmaps.databinding.FragmentPointBinding
import ru.netology.yandexmaps.ui.dto.PointDto
import ru.netology.yandexmaps.ui.fragment.MapsFragment.Companion.pointDtoData
import ru.netology.yandexmaps.ui.fragment.NewPointFragment.Companion.pointData
import ru.netology.yandexmaps.ui.util.StringArg
import kotlin.getValue
import kotlin.jvm.java
import ru.netology.yandexmaps.ui.viewmodel.PointViewModel

class PointFragment : Fragment() {

    companion object {
        private var pointDto = PointDto(
            id = 0,
            title = "",
            city = "",
            latitude = 0.0,
            longitude = 0.0,
            detailedInformation = "",
            photo = ""
        )

        private val gson = Gson()
        private var pointId: Long = 0
        var Bundle.textPoint by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPointBinding.inflate(layoutInflater, container, false)
        val viewModel: PointViewModel by activityViewModels()
        applyInset(binding.pointFragment)

        arguments?.textPoint?.let {
            pointDto = gson.fromJson(it, PointDto::class.java)
            pointId = pointDto.id
            arguments?.textPoint = null
        }

        with(binding) {
            title.text = pointDto.title
            city.text = pointDto.city
            latitude.text = pointDto.latitude.toString()
            longitude.text = pointDto.longitude.toString()
            detailedInformation.text = pointDto.detailedInformation
            photo.setImageURI(pointDto.photo?.toUri())

            location.setOnClickListener {
                findNavController().navigate(
                    R.id.action_pointFragment2_to_mapsFragment,
                    Bundle().apply {
                        pointDtoData = gson.toJson(pointDto)
                    }
                )
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_point)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.remove -> {
                                viewModel.removeById(pointDto.id)
                                findNavController().navigateUp()
                                true
                            }

                            R.id.edit -> {
                                viewModel.editById(pointDto)
                                findNavController().navigate(
                                    R.id.action_pointFragment2_to_newPointFragment,
                                    Bundle().apply {
                                        pointData = gson.toJson(pointDto)
                                    }
                                )
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.data.collectLatest {
                        it.map { point ->
                            if (point.id == pointId) {
                                pointDto = point
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun applyInset(main: View) {
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight,
                if (isImeVisible) imeInsets.bottom else systemBars.bottom)
            insets
        }
    }
}