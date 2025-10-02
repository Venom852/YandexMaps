package ru.netology.yandexmaps.ui.fragment

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.yandexmaps.R
import ru.netology.yandexmaps.databinding.FragmentNewPointBinding
import ru.netology.yandexmaps.ui.dao.PointDao
import ru.netology.yandexmaps.ui.dto.PointDto
import ru.netology.yandexmaps.ui.entity.PointDraftEntity
import ru.netology.yandexmaps.ui.util.AndroidUtils
import ru.netology.yandexmaps.ui.util.StringArg
import kotlin.getValue
import ru.netology.yandexmaps.ui.viewmodel.PointViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NewPointFragment : Fragment() {
    @Inject
    lateinit var dao: PointDao

    companion object {
        var pointDto = PointDto(
            id = 0,
            title = "",
            city = "",
            latitude = 0.0,
            longitude = 0.0,
            detailedInformation = "",
            photo = ""
        )
        var pointDraftEntity = PointDraftEntity(
            id = 0L,
            title = null,
            city = null,
            latitude = null,
            longitude = null,
            detailedInformation = null
        )

        private var editing = false
        private val gson: Gson = Gson()
        var Bundle.pointData by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNewPointBinding.inflate(layoutInflater, container, false)
        val viewModel: PointViewModel by activityViewModels()

        arguments?.pointData?.let {
            pointDto = gson.fromJson(it, PointDto::class.java)
            binding.enterTitle.setText(pointDto.title)
            binding.enterCity.setText(pointDto.city)
            binding.enterLatitude.setText(pointDto.latitude.toString())
            binding.enterLongitude.setText(pointDto.longitude.toString())
            binding.enterDetailedInformation.setText(pointDto.detailedInformation)
            editing = true
            arguments?.pointData = null
        }

        lifecycleScope.launch {
            if (!dao.isEmptyDraft() && !editing) {
                pointDraftEntity = dao.getDraft()
                binding.enterTitle.setText(pointDraftEntity.title)
                binding.enterCity.setText(pointDraftEntity.city)
                binding.enterLatitude.setText(pointDraftEntity.latitude)
                binding.enterLongitude.setText(pointDraftEntity.longitude)
                binding.enterDetailedInformation.setText(pointDraftEntity.detailedInformation)
                dao.removeDraft()
            }
        }

        binding.enterTitle.requestFocus()

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changePhoto(uri)
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.changePhoto(null)
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.groupPhotoContainer.visibility = View.GONE
                return@observe
            }

            binding.groupPhotoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }

        binding.menu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.options_new_point)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.save -> {
                            with(binding) {
                                if (!enterTitle.text.isNullOrBlank() &&
                                    !enterLatitude.text.isNullOrBlank() &&
                                    !enterLongitude.text.isNullOrBlank()
                                ) {
                                    viewModel.savePoint(
                                        enterTitle.text.toString(),
                                        enterCity.text.toString(),
                                        enterLatitude.text.toString().toDouble(),
                                        enterLongitude.text.toString().toDouble(),
                                        enterDetailedInformation.text.toString()
                                    )
                                    AndroidUtils.hideKeyboard(requireView())
                                }
                                viewModel.edited.value = viewModel.empty
                                findNavController().navigateUp()
                            }
                            true
                        }

                        R.id.cancel -> {
                            viewModel.edited.value = viewModel.empty
                            findNavController().navigateUp()
                            true
                        }

                        else -> false
                    }
                }
            }.show()
        }

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!isEmpty(binding.enterTitle.text.toString()) && !editing ||
                !isEmpty(binding.enterCity.text.toString()) && !editing ||
                !isEmpty(binding.enterLatitude.text.toString()) && !editing ||
                !isEmpty(binding.enterLongitude.text.toString()) && !editing ||
                !isEmpty(binding.enterDetailedInformation.text.toString()) && !editing
            ) {
                lifecycleScope.launch {
                    dao.insertDraft(
                        PointDraftEntity(
                            pointDraftEntity.id,
                            binding.enterTitle.text.toString(),
                            binding.enterCity.text.toString(),
                            binding.enterLatitude.text.toString(),
                            binding.enterLongitude.text.toString(),
                            binding.enterDetailedInformation.text.toString()
                        )
                    )
                }
            }
            editing = false
            viewModel.edited.value = viewModel.empty
            findNavController().navigateUp()
        }

        callback.isEnabled

        return binding.root
    }
}