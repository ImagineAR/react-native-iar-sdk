package com.iarsdk

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext

class IarSdkPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return listOf(IarSdkModule(reactContext))
  }

override fun createViewManagers(
      reactContext: ReactApplicationContext
  ) = listOf(SurfaceViewManager())
}

