package com.iarsdk

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout

import com.facebook.react.ReactApplication
import com.iar.surface_sdk.aractivity.IARSurfaceActivity
import java.lang.ref.WeakReference

class SurfaceARActivity : IARSurfaceActivity() {

  private var fragmentOverlay: WeakReference<SurfaceARActivityFragment>? = null
  private var buttonPlaceConfig: HashMap<String, Any> = hashMapOf<String, Any>()

  private lateinit var iarSdkModule: IarSdkModule

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    Log.i("SurfaceARActivity", "SurfaceARActivity onCreate")

    // Get Intent Extras
    buttonPlaceConfig = intent.getSerializableExtra(ARG_PLACE_BUTTON_CONFIG) as HashMap<String, Any>

    fragmentOverlay?.get()?.setPlaceButtonStyle(buttonPlaceConfig)

    // Get the instance of IarSdkModule
    val reactInstanceManager =
      (applicationContext as ReactApplication).reactNativeHost.reactInstanceManager
    val reactContext = reactInstanceManager.currentReactContext
    if (reactContext!!.getNativeModule(IarSdkModule::class.java) != null) {
      iarSdkModule =
        reactContext!!.getNativeModule(IarSdkModule::class.java) as IarSdkModule
    } else {
      Log.i("SurfaceARActivity", "reactContext!!.getNativeModule(IarSdkModule) is null")
    }

    // Callback and let the IarSdkModule know SurfaceArActivity was created and pass back a reference to this.
    iarSdkModule?.onSurfaceARCreated(this)

    if (!this.isARCoreSupported()) {
      Log.i("SurfaceARActivity", "This device does not support ARCore.")
    }
  }

  /**
   * Here we can override the function provided by IARActivity
   * to add our custom overlay over the camera view.
   * This can provide better customization to the UI.
   * Typically this overlay can have a targeting
   * reticule or help text to provide more context
   * on what the user should do.
   */
  override fun onSetupOverlayViews(contentView: FrameLayout) {
    val overlayFragment = SurfaceARActivityFragment()

     //Add our custom overlay fragment to the container view on top of the camera view.
    contentView.let {
      val fm = this.supportFragmentManager
      val ft = fm.beginTransaction()
      ft.add(it.id, overlayFragment).commit()

     //Set a reference to our fragment so we can propagate IARActivity events
     //to trigger UI states.
      fragmentOverlay = WeakReference(overlayFragment)
    }
  }

  /**
   * We can override this to detect when an asset is anchored or not.
   * We just pass this to the overlay which should handle the logic of what to display.
   */
  override fun onAssetAnchored(isPlaced: Boolean) {
    super.onAssetAnchored(isPlaced)
    Log.i("SurfaceARActivity", "onAssetAnchored: $isPlaced")
    iarSdkModule?.onAssetAnchored(isPlaced)
    fragmentOverlay?.get()?.onAssetAnchored(isPlaced)
  }

  override fun surfaceViewSurfaceDetected() {
    Log.i("SurfaceARActivity", "surfaceViewSurfaceDetected")
    iarSdkModule?.surfaceDetected()
    fragmentOverlay?.get()?.surfaceDetected()
  }

  public fun logThis(logString: String) {
    Log.i("SurfaceARActivity", "logThis: $logString")
  }
  companion object {
    const val ARG_PLACE_BUTTON_CONFIG: String = "arg_place_button_config"
  }
}
