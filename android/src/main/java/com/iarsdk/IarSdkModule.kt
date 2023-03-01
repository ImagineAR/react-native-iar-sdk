package com.iarsdk

// Import Android
import com.google.gson.Gson
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

// Import React Native
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray

// Import ImagineAR SDK
import com.iar.iar_core.CoreAPI
import com.iar.iar_core.Marker
import com.iar.iar_core.Region
import com.iar.iar_core.Reward
import com.iar.iar_core.User
import com.iar.surface_sdk.AssetInfo
import com.iar.surface_sdk.SurfaceAPI
import com.iar.surface_sdk.aractivity.IARSurfaceActivity

@ReactModule(name = IarSdkModule.NAME)
class IarSdkModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private lateinit var surfaceARActivity: SurfaceARActivity

  private val gson = Gson()
  private val applicationContext = reactApplicationContext.applicationContext
  private val REGION_KEY = Region.NA;

  private var ORG_KEY: String = "";
  private var EXTERNAL_USER_ID = "";
  private var placeButtonConfig: HashMap<String, Any> = hashMapOf<String, Any>()

  // Validate License
  @ReactMethod
  fun initialize(orgKey: String, promise: Promise) {
        // First we initialize the Core API.

        try {
          CoreAPI.initialize(orgKey, REGION_KEY, applicationContext)
          ORG_KEY = orgKey

          promise.resolve("License successfully validated")
        } catch (error: Throwable) {
          promise.reject("License key not valid: ", error)
        }
    }

  // Return License used
  @ReactMethod
  fun iarLicense(promise: Promise) {

    if(ORG_KEY !== "") {
      promise.resolve(ORG_KEY)
    } else {
      promise.reject("License not found")
    }

  }
  // Set User ID
  @ReactMethod
  fun createExternalUserId(userId: String, promise: Promise) {
        CoreAPI.createExternalUserId(
            User(userId),
            {

              setExternalUserId(userId, promise)
                promise.resolve("UserID Set: $userId")
            },
            { errorCode, errorMessage ->
                promise.reject("error setting UserID | Code: $errorCode | Message: $errorMessage")

            }
        )
  }

  // Set User ID
  @ReactMethod
  fun setExternalUserId(userId: String, promise: Promise) {
    try {
      CoreAPI.setExternalUserId(userId, true)
      EXTERNAL_USER_ID = CoreAPI.getCurrentExternalUserId() as String

      SurfaceAPI.validateLicense(
        ORG_KEY,
        REGION_KEY,
        applicationContext
      )

      Log.i("IarSdkModule", "setExternalUserId - externalUserId: $EXTERNAL_USER_ID")

      promise.resolve(EXTERNAL_USER_ID)
    } catch (error: Throwable) {
        promise.reject("Unable to set user ID: ", userId)
    }
  }

  // Get User ID
  @ReactMethod
  fun getExternalUserId(promise: Promise) {
    try {
      EXTERNAL_USER_ID = CoreAPI.getCurrentExternalUserId() as String

      Log.i("IarSdkModule", "getExternalUserId - externalUserId: $EXTERNAL_USER_ID")
      promise.resolve(EXTERNAL_USER_ID)
    } catch (error: Throwable) {
        promise.reject("Unable to get user ID from SDK")
    }
  }


  // fetch OnDemand Markers under the current OrgKey
  @ReactMethod
      fun downloadOnDemandMarkers(promise: Promise) {
        CoreAPI.getDemandMarkers("OnDemand",
            { markers: List<Marker> ->
              val array: WritableArray = WritableNativeArray()

              for (marker in markers) {
                val map: WritableMap = WritableNativeMap()
                map.putString("image", marker.previewImageUrl)
                map.putString("name", marker.name)
                map.putString("id", marker.id)
                array.pushMap(map)
              }

              promise.resolve(array)
            })
        { errorCode, errorMessage ->
            promise.reject("OnDemand Markers Error | Code: $errorCode | Message: $errorMessage")
        }
    }

  @ReactMethod
  fun getUserRewards(promise: Promise) {
    CoreAPI.getRewardsForCurrentUser(
      { rewards: List<Reward> ->
        val rewardsArray: WritableArray = WritableNativeArray()

        for (reward in rewards) {
          val rewardMap: WritableMap = WritableNativeMap()
          rewardMap.putString("id", reward.id)
          rewardMap.putString("name", reward.name)
          rewardMap.putString("image", reward.image.url)
          rewardMap.putString("rewardReasonType", reward.rewardReasonType)
          rewardMap.putString("type", reward.type)
          rewardMap.putBoolean("actionButtonEnabled", reward.actionButtonEnabled)
          rewardMap.putString("actionButtonText", reward.actionButtonText)
          rewardMap.putString("actionButtonUrl", reward.actionButtonUrl)
          rewardMap.putString("generalPromoCode", reward.generalPromoCode?.promoCode)
          rewardMap.putString("generalPromoCodeOptionalText", reward.generalPromoCode?.optionalText)
          rewardsArray.pushMap(rewardMap)
        }

        promise.resolve(rewardsArray)
      }
    )
    { errorCode, errorMessage ->
      promise.reject("getUserRewards Error | Code: $errorCode | Message: $errorMessage")
    }
  }

  //// SurfaceAR Methods
  @ReactMethod
  fun setMarkerId(markerId: String) {
    Log.i("IarSdkModule", "SurfaceViewModule - MarkerId: $markerId")
    getMarkerById(markerId);
  }

  fun downloadDemandAssetsAndRewards(
    marker: Marker,
  ) {
    val activity = currentActivity as AppCompatActivity
    SurfaceAPI.downloadDemandAssetsAndRewards(
      activity,
      marker,
      onSuccess = { assetInfo: AssetInfo ->
        startSurfaceActivity(assetInfo, marker)
        rewardsAwarded(marker.rewards)
      },
      onFail = { errorMsg ->
        Log.i("IarSdkModule", "downloadDemandAssetsAndRewards: Error: $errorMsg")
      },
      onProgress = { progress ->
        // println("progress $progress")
        val params = Arguments.createMap().apply {
          putInt("progress", progress)
        }
        sendEvent(reactContext, "markerDownloadProgress", params)
      }
    )
  }

  fun setPlaceButtonConfig(buttonConfig: ReadableMap) {
    placeButtonConfig = buttonConfig.toHashMap()
  }

  fun getMarkerById(
    markerId: String
  ) {
    Log.i("IarSdkModule","getMarkerById: $markerId")
    var externalUserId = CoreAPI.getCurrentExternalUserId()
    Log.i("IarSdkModule", "getMarkerById - externalUserId: $externalUserId")
    SurfaceAPI.getMarkerById(markerId,
      { marker: Marker ->
        downloadDemandAssetsAndRewards(marker)
      })
    { errorCode, errorMessage ->
      Log.i("IarSdkModule","getMarkerById - Error: $errorCode | Message: $errorMessage | MarkerId: $markerId")
    }
  }

  fun startSurfaceActivity(assetInfo: AssetInfo, marker: Marker) {
      val intent = Intent(reactContext, SurfaceARActivity::class.java).apply {
        putExtras(assetInfo.toExtrasBundle())
        putExtra(IARSurfaceActivity.ARG_MARKER, gson.toJson(marker))
        putExtra(SurfaceARActivity.ARG_PLACE_BUTTON_CONFIG, placeButtonConfig)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      reactContext?.startActivity(intent)

  }

  //// Location Marker Methods
  @ReactMethod
  fun getLocationMarkers(latitude: Double, longitude: Double, radius: Int, promise: Promise) {
    SurfaceAPI.getLocationMarkers(
      latitude,
      longitude,
      radius,
      { markers: List<Marker> ->
        val locationMarkerArray: WritableArray = WritableNativeArray()

        for (marker in markers) {
          val map: WritableMap = WritableNativeMap()
          map.putString("image", marker.previewImageUrl)
          map.putString("name", marker.name)
          map.putString("id", marker.id)
          map.putDouble("latitude", marker.location.latitude)
          map.putDouble("longitude", marker.location.longitude)
          map.putDouble("distance", marker.location.distance)
          map.putInt("radius", marker.location.radius)

          locationMarkerArray.pushMap(map)
        }
        promise.resolve(locationMarkerArray)
      }
    )
    { errorCode: Int?, errorMessage: String? ->
      Log.i("IarSdkModule","getLocationMarkers - Error: $errorCode | Message: $errorMessage")
    }
  }


  //////////////// Event setup
  private fun sendEvent(reactContext: ReactApplicationContext, eventName: String, params: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  private var listenerCount = 0

  @ReactMethod
  fun addListener(eventName: String) {
    if (listenerCount == 0) {
      // Set up any upstream listeners or background tasks as necessary
    }

    listenerCount += 1
  }

  @ReactMethod
  fun removeListeners(count: Int) {
    listenerCount -= count
    if (listenerCount == 0) {
      // Remove upstream listeners, stop unnecessary background tasks
    }
  }

  //// SurfaceARActivity Methods
  fun onSurfaceARCreated(sar: SurfaceARActivity) {
    Log.i("IarSdkModule","SurfaceARActivity been created and referenced")
    surfaceARActivity = sar;
  }

  fun onAssetAnchored(isAnchored: Boolean) {
    Log.i("IarSdkModule","onAssetAnchored: $isAnchored")
    val params = Arguments.createMap().apply {
      putBoolean("isAnchored", isAnchored)
    }
    sendEvent(reactContext, "isAssetAnchored", params)
  }

  fun surfaceDetected() {
    Log.i("IarSdkModule","surfaceDetected")
    val params = Arguments.createMap().apply {
      putBoolean("surfaceDetected", true)
    }
    sendEvent(reactContext, "surfaceDetected", params)
  }

  private fun rewardsAwarded(rewards: List<Reward>) {
    // Don't send an event if the user has not received any new rewards
    if(rewards.isEmpty()) {
      return
    }

    Log.i("IarSdkModule","rewardsAwarded")
    val writableArray = WritableNativeArray()
    for (reward in rewards) {
      writableArray.pushString(reward.id)
    }

    val rewardMap: WritableMap = WritableNativeMap()
    rewardMap.putArray("rewards", writableArray)

    sendEvent(reactContext, "rewardsAwarded", rewardMap)
  }

  // Misc

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "IarSdk"
  }
}
