package ru.netology.yandexmaps.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.yandexmaps.R
import ru.netology.yandexmaps.databinding.FragmentListPointsBinding
import ru.netology.yandexmaps.ui.adapter.OnInteractionListener
import ru.netology.yandexmaps.ui.adapter.PointAdapter
import ru.netology.yandexmaps.ui.dto.PointDto
import ru.netology.yandexmaps.ui.fragment.NewPointFragment.Companion.pointData
import ru.netology.yandexmaps.ui.viewmodel.PointViewModel
import kotlin.getValue

//@AndroidEntryPoint
class ListPointsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentListPointsBinding.inflate(layoutInflater, container, false)
        val viewModel: PointViewModel by activityViewModels()
        val gson = Gson()

        applyInset(binding.listPoints)

        val adapter = PointAdapter(object : OnInteractionListener {
            override fun onRemove(pointDto: PointDto) {
                viewModel.removeById(pointDto.id)
            }

            override fun onEdit(pointDto: PointDto) {
                viewModel.editById(pointDto)
                findNavController().navigate(
                    R.id.action_listPointsFragment2_to_newPointFragment,
                    Bundle().apply {
                        pointData = gson.toJson(pointDto)
                    }
                )
            }
        })

        binding.listPoints.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest {
                    val newPost = adapter.currentList.size < it.size
                    adapter.submitList(it) {
                        if (newPost) {
                            binding.listPoints.smoothScrollToPosition(0)
                        }
                    }
                }
            }
        }

        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_listPointsFragment2_to_newPointFragment)
        }

        return binding.root
    }

    private fun applyInset(main: View) {
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                if (isImeVisible) imeInsets.bottom else systemBars.bottom
            )
            insets
        }
    }
}