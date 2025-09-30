package ru.netology.yandexmaps.ui.activity

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import ru.netology.yandexmaps.BuildConfig


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPS_API_KEY)
    }
}