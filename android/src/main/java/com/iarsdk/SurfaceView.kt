package com.iarsdk

// Import Android
import android.util.Log
import android.content.Context
import android.widget.FrameLayout

// Import React Native
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.bridge.ReadableMap

class SurfaceView(context: Context) : FrameLayout(context) {

  private val reactContext = context as ReactContext
  private val iarSdkModule = reactContext.getNativeModule(IarSdkModule::class.java)

  init {
    // Add the native Android activity to the view
    Log.i("SurfaceView", "Surface View Init")
  }
  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    Log.d("SurfaceView", "onAttachedToWindow called")
  }

  @ReactProp(name = "markerId")
  fun setMarkerId(markerId: String) {
    iarSdkModule?.getMarkerById(markerId)
  }

  @ReactProp(name = "placeButtonConfig")
  fun setPlaceButtonConfig(placeButtonConfig: ReadableMap) {
    iarSdkModule?.setPlaceButtonConfig(placeButtonConfig)
  }


}
