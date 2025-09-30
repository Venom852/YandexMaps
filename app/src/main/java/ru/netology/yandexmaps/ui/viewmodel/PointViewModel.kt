package ru.netology.yandexmaps.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.yandexmaps.ui.dao.PointDao
import ru.netology.yandexmaps.ui.dto.PointDto
import ru.netology.yandexmaps.ui.entity.PointEntity
import ru.netology.yandexmaps.ui.entity.toDto
import ru.netology.yandexmaps.ui.model.PhotoModel
import javax.inject.Inject

@HiltViewModel
class PointViewModel @Inject constructor(
    private val dao: PointDao
) : ViewModel() {
    val empty = PointDto(
        id = 0,
        title = "",
        city = null,
        latitude = 0.0,
        longitude = 0.0,
        detailedInformation = null,
        photo = null
    )

    private val noPhoto = PhotoModel()
    val data: Flow<List<PointDto>> = dao.getAll().map { it.toDto() }
    val edited = MutableLiveData(empty)
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    fun removeById(id: Long) {
        viewModelScope.launch {
            dao.removeById(id)
        }
    }

    fun savePoint(
        title: String,
        city: String,
        latitude: Double,
        longitude: Double,
        detailedInformation: String
    ) {
        edited.value?.let {
            viewModelScope.launch {

                var pointDto = it.copy(
                    title = title,
                    city = city,
                    latitude = latitude,
                    longitude = longitude,
                    detailedInformation = detailedInformation
                )

                if (_photo.value?.uri != null) {
                    _photo.value?.uri?.let { uri ->
                        pointDto = pointDto.copy(
                            photo = uri.toString()
                        )
                    }
                    dao.save(PointEntity.fromDto(pointDto))
                } else {
                    dao.save(PointEntity.fromDto(pointDto))
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun savePoints(mutableListPointDto: MutableList<PointDto>) {
        viewModelScope.launch {
            mutableListPointDto.forEach {
                val pointDto = it.copy(
                    title = it.title,
                    latitude = it.latitude,
                    longitude = it.longitude
                )
                dao.save(PointEntity.fromDto(pointDto))
            }
        }
    }

    fun editById(pointDto: PointDto) {
        edited.value = pointDto
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }
}