package com.iarsdk

// Import Android
import android.util.Log

// Import React Native
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.bridge.ReadableMap

class SurfaceViewManager : SimpleViewManager<SurfaceView>() {
    companion object {
        const val REACT_CLASS = "SurfaceView"
    }

    override fun getName(): String {
        return REACT_CLASS
    }

    override fun createViewInstance(reactContext: ThemedReactContext): SurfaceView {
        return SurfaceView(reactContext)
    }

  @ReactProp(name = "markerId")
  fun setMarkerId(view: SurfaceView, markerId: String) {
    view.setMarkerId(markerId)
  }
  @ReactProp(name = "placeButtonConfig")
  fun setPlaceButtonConfig(view: SurfaceView, placeButtonConfig: ReadableMap) {
    view.setPlaceButtonConfig(placeButtonConfig)
  }
}
