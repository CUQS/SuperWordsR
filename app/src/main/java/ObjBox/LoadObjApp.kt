package ObjBox

import android.app.Application

class LoadObjApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}