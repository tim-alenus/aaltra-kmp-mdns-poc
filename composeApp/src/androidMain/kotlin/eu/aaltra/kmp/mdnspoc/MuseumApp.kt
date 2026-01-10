package eu.aaltra.kmp.mdnspoc

import android.app.Application
import eu.aaltra.kmp.mdnspoc.di.initKoin

class MuseumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
