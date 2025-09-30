package ru.netology.yandexmaps.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.yandexmaps.R
import ru.netology.yandexmaps.databinding.FragmentMapsBinding
import ru.netology.yandexmaps.databinding.TitlePointDialogBoxBinding
import ru.netology.yandexmaps.ui.dto.PointDto
import ru.netology.yandexmaps.ui.extensions.DrawableImageProvider
import ru.netology.yandexmaps.ui.extensions.ImageInfo
import ru.netology.yandexmaps.ui.fragment.NewPointFragment.Companion.pointData
import ru.netology.yandexmaps.ui.util.StringArg
import ru.netology.yandexmaps.ui.viewmodel.PointViewModel
import kotlin.getValue

//@AndroidEntryPoint
class MapsFragment : Fragment() {
    companion object {
        var Bundle.pointDtoData by StringArg
    }

    private lateinit var yandexMap: Map
    private lateinit var mapKit: MapKit
    private lateinit var placemarkMapObject: PlacemarkMapObject
    private lateinit var polyline: Polyline
    private lateinit var polylineMapObject: PolylineMapObject
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bindingTitlePointDialogBox: TitlePointDialogBoxBinding
    private lateinit var binding: FragmentMapsBinding
    private lateinit var locationManager: LocationManager
    private var listPoints = mutableListOf<PointDto>()
    private val zoomSteep = 6f
    private val smoothAnimation = Animation(Animation.Type.SMOOTH, 3F)
    private val points = mutableListOf<Point>()
    private val gson: Gson = Gson()
    private var condition = 0

    private var pointDto = PointDto(
        id = 0,
        title = "",
        city = null,
        latitude = 0.0,
        longitude = 0.0,
        detailedInformation = null,
        photo = null
    )

    private val placemarkTapListener = MapObjectTapListener { mapObject, point ->
        deleteMarker(mapObject)
        true
    }

    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            val target = Point(point.latitude, point.longitude)
            addMarker(map, target)
            moveToMarker(map, target)
        }

        override fun onMapLongTap(map: Map, point: Point) {

        }
    }

    private val geoObjectTapListener = GeoObjectTapListener {
        val point = it.geoObject.geometry.firstOrNull()?.point ?: return@GeoObjectTapListener true
        yandexMap.cameraPosition.run {
            val position = CameraPosition(point, zoom, azimuth, tilt)
            yandexMap.move(position, smoothAnimation, null)
        }

        val selectionMetadata =
            it.geoObject.metadataContainer.getItem(GeoObjectSelectionMetadata::class.java)
        yandexMap.selectGeoObject(selectionMetadata)
        Toast.makeText(
            requireContext(),
            ("${it.geoObject.name}"),
            Toast.LENGTH_LONG
        ).show()

        true
    }

    val locationListener = object : LocationListener {
        override fun onLocationUpdated(location: Location) {
            val target =
                Point(location.position.latitude, location.position.longitude)
            addMarker(yandexMap, target)
            moveToMarker(yandexMap, target)
        }

        override fun onLocationStatusUpdated(p0: LocationStatus) {
            println(p0)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingTitlePointDialogBox = TitlePointDialogBoxBinding.inflate(layoutInflater, container, false)
        binding = FragmentMapsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapView = binding.map
        val mapWindow = mapView.mapWindow
        val viewModel: PointViewModel by activityViewModels()

        dialog = BottomSheetDialog(requireContext())
        yandexMap = mapWindow.map
        mapKit = MapKitFactory.getInstance()
        placemarkMapObject = yandexMap.mapObjects.addPlacemark()
        polylineMapObject = yandexMap.mapObjects.addPolyline()
        locationManager = mapKit.createLocationManager()

        yandexMap.addInputListener(inputListener)
        yandexMap.addTapListener(geoObjectTapListener)
        mapKit.setLocationManager(locationManager)


        arguments?.pointDtoData?.let {
            pointDto = gson.fromJson(it, PointDto::class.java)
            addMarker(yandexMap, Point(pointDto.latitude, pointDto.longitude))
            moveToMarker(yandexMap, Point(pointDto.latitude, pointDto.longitude))
            arguments?.pointData = null
        }

        subscribeToLifecycle(mapView)
        checkPermissions(mapWindow)

        with(binding) {
            geoLocation.setOnClickListener {
                locationManager.requestSingleUpdate(locationListener)
            }

            addPoints.setOnClickListener {
                viewModel.savePoints(listPoints)
                findNavController().navigate( R.id.action_mapsFragment_to_listPointsFragment2 )
            }

            goOverListPoint.setOnClickListener{
                findNavController().navigate(R.id.action_mapsFragment_to_listPointsFragment2)
            }

            route.setOnClickListener {
                if (condition == 0) {
                    addPolyline()
                    ++condition
                } else {
                    deletePolyline()
                    condition = 0
                }
            }

            zoomMinus.setOnClickListener { changingTheScale(-zoomSteep) }
            zoomPlus.setOnClickListener { changingTheScale(zoomSteep) }
        }
    }

    private fun changingTheScale(value: Float) {
        with(yandexMap.cameraPosition) {
            yandexMap.move(
                CameraPosition(target, zoom + value, azimuth, tilt),
                smoothAnimation,
                null,
            )
        }
    }

    private fun moveToMarker(
        yandexMap: Map,
        target: Point
    ) {
        val currentPosition = yandexMap.cameraPosition
        yandexMap.move(
            CameraPosition(
                target, 15F, currentPosition.azimuth, currentPosition.tilt,
            ),
            smoothAnimation,
            null,
        )
    }

    private fun deletePolyline() {
        yandexMap.mapObjects.remove(polylineMapObject)
    }

    private fun addPolyline() {
        listPoints.forEach {
            points.add(Point(it.latitude, it.longitude))
        }
        polyline = Polyline(points)
        polylineMapObject = yandexMap.mapObjects.addPolyline(polyline)
    }

    private fun deleteMarker(mapObject: MapObject) {
        listPoints.filter { it.title != mapObject.userData }
        yandexMap.mapObjects.remove(mapObject)
    }

    private fun addMarker(yandexMap: Map, target: Point) {
        val imageProvider =
            DrawableImageProvider(requireContext(), ImageInfo(R.drawable.ic_netology_48dp))
        var title = ""

        if (pointDto.latitude == target.latitude && pointDto.longitude == target.longitude) {
            title = pointDto.title
            placemarkMapObject = yandexMap.mapObjects.addPlacemark {
                it.setIcon(imageProvider)
                it.geometry = target
                it.setIcon(imageProvider)
                it.setText(title)
                it.addTapListener(placemarkTapListener)
                it.userData = title
            }
        } else {
            dialog.setCancelable(false)
            dialog.setContentView(bindingTitlePointDialogBox.root )
            dialog.show()

            bindingTitlePointDialogBox.enterTitleButton.setOnClickListener {
                title = bindingTitlePointDialogBox.enterTitleText.text.toString()
                placemarkMapObject = yandexMap.mapObjects.addPlacemark {
                    it.setIcon(imageProvider)
                    it.geometry = target
                    it.setIcon(imageProvider)
                    it.setText(title)
                    it.addTapListener(placemarkTapListener)
                    it.userData = title
                }
                listPoints.add(pointDto.copy(title = title, latitude = target.latitude, longitude = target.longitude))
                dialog.dismiss()
            }
        }
    }

    private fun checkPermissions(mapWindow: MapWindow) {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    enableUserLocation(mapWindow)
                } else {
                    // TODO: show sorry dialog
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableUserLocation(mapWindow)
            }
            // 2. Должны показать обоснование необходимости прав
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // TODO: show rationale dialog
            }
            // 3. Запрашиваем права
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun enableUserLocation(mapWindow: MapWindow) {
        val userLocation = mapKit.createUserLocationLayer(mapWindow)
        userLocation.isVisible = true
        userLocation.isHeadingModeActive = true
    }

    private fun subscribeToLifecycle(mapView: MapView) {
        viewLifecycleOwner.lifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(
                    source: LifecycleOwner,
                    event: Lifecycle.Event
                ) {
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            mapKit.onStart()
                            mapView.onStart()
                        }

                        Lifecycle.Event.ON_STOP -> {
                            mapView.onStop()
                            mapKit.onStop()
                        }

                        Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)

                        else -> Unit
                    }
                }
            }
        )
    }
}