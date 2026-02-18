package com.just_for_fun.fileflip

import android.app.Application
import com.just_for_fun.fileflip.data.DemoFilesData
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FileFlipApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DemoFilesData.initialize(this)
    }
}
