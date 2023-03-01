
# react-native-iar-sdk

A React Native wrapper for the ImagineAR Surface SDK

This guide assumes you already have a React Native iOS/Android application, to which you will be adding the ImagineAR SDK.

The ImagineAR SDK is not compatible with React Native web solutions.

# Installation

```sh

yarn install react-native-iar-sdk

```
## Extra Steps for IOS
The recommended minimum iOS version is `13.0`

#### Install pods for ios

```sh
 
cd ios && pod install --repo-update

```

#### Add privacy permissions to Info.plist

In your app’s  `Info.plist`  file, add entries for the following keys:

```text

NSCameraUsageDescription
NSMicrophoneUsageDescription
NSPhotoLibraryAddUsageDescription
NSLocationWhenInUseUsageDescription 

```
The values that you put for these will be displayed to the user when approving Camera, Microphone, Location, and Photo Library access, so you can choose what you would like displayed (for example “Required for Augmented Reality” and “Required to save augmented reality reward images”). Note: the location permission is only needed for location-based AR experiences and the microphone permission is only needed to record a video from an AR experience. If you are not using these features, you can omit the related permission.

#### React Native Config for iOS
In the podfile of your project, the flag `use_frameworks!` needs to be set, along with disabling `hermes` and `fabric` if applicable.
Setting cocoapods to not use deterministic uuids is also required.
```ruby

...
platform :ios, '13.0'
install! 'cocoapods', :deterministic_uuids => false
use_frameworks!
...
target 'MyARApp' do

config = use_native_modules!
flags = get_default_flags()
...
use_react_native!(
  :path  => config[:reactNativePath],
  :hermes_enabled  =>  false,
  :fabric_enabled  =>  false,
  ...
)
...
end

```
## Extra Steps for Android

#### Gradle configuration
The minimum API Level for this the IAR SDK is `26`
```gradle

buildscript {
  ext {
    ...
    minSdkVersion =  26
  }
  ...
}

```
The IAR SDK is built using Kotlin. If your application is not already using Kotlin, you will need to add the Kotlin plugin:

```gradle

buildscript {
  ext.kotlin_version = '1.5.20'
  repositories {
      ...
  }
  dependencies {
    ...
    classpath 'com.android.tools.build:gradle:4.1.2'
    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
  }
}

```
In your android tag in the app module's build.grade file, add the following `aaptOptions` and `noCompress` values.

```gradle

android {
  compileSdkVersion ...
  buildToolsVersion ...
  defaultConfig {...}
  ...
  ...
  // We use the .filamat extension for materials compiled with matc
  // Telling aapt to not compress them allows to load them efficiently
  aaptOptions {
      noCompress 'filamat', 'ktx'
  }
}

```
#### AndroidManifest config

In your app’s  `AndroidManifest.xml`  file, add the following entries (if they do not already exist):

```xml

<uses-feature android:glEsVersion="0x00020000" />
<uses-feature android:name="android.hardware.camera" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

```
You may also need to add ARCore `meta-data` element

```xml

<application ... >
  ...
  <meta-data
    android:name="com.google.ar.core"
    android:value="optional" 
  />
</application>

```
#### Proguard Rules
If your project uses Proguard, the following rules are recommended:

```Proguard

# Add any project specific keep options here:
-keep class com.google.android.filament.** { *; }
-keep interface com.google.android.filament.** { *; }
-keep class com.vuforia.** { *; }
-keep interface com.vuforia.** { *; }
-keep class com.google.ar.core.** { *; }
-keep interface com.google.ar.core.** { *; }
-keep class ca.iversoft.iar_core.** { *; }
-keep interface ca.iversoft.iar_core.** { *; }
-keep class com.iar.** { *; }
-keep interface com.iar.** { *; }

```

# Configuration

## Validate your license
You will be given an ImagineAR license that you will need to provide the SDK. This needs to be done before any of the ImagineAR SDK functionality is used as it will error out without a correct license file.

For demonstration purposes, you can try the DEMO license:

`pk_org_d5f1fca52da847c9a1a064619b91c74e`

The demo license above contains a few sample markers, hunts and other assets.

```tsx

import { initialize } from 'react-native-iar-sdk';

const response = await initialize('pk_org_d5f1fca52da847c9a1a064619b91c74e');  

// Log out connection response
console.log('initialization response: ', response);

```
# Workflow
To use the IAR SDK, first a user needs to be created or, if the user already exists, it needs to be set in the library. The userid is a alphanumeric string of your choice, and will save that users rewards to their 'account'

## Create a New User
```tsx

import { createExternalUserId } from 'react-native-iar-sdk';

const response = await createExternalUserId(userId);

console.log('createExternalUserId response', response);

```

if the user already exists, you can just set them in the library using `setExternalUserId`

## Set Existing User
```tsx

import { setExternalUserId } from 'react-native-iar-sdk';

setExternalUserId(userId);

```

After the user has been created/set you are ready to download a list of available markers

## Download OnDemand Markers
On-Demand Markers allow you to create custom user experiences and supplement them with AR visuals supplied by the ImagineAR Cloud

If you like, you can download an array of markers from the SDK. 

```tsx

import { downloadOnDemandMarkers, IARMarker } from 'react-native-iar-sdk';

const markers: IARMarker[] = await downloadOnDemandMarkers();

```

The markers are returned in an array of type `IARMarker` which is defined as:

```tsx

{
  id:  string;
  name:  string;
  image:  string;
}

```

### Asset Types

The ImagineAR SDK can render the following digital content types in the AR camera experiences:

**3D Models**

-   Providing the most immersive experience, the SDK supports complex 3D model rendering
-   Animation is supported for 3D models, looping the animation in the scene
-   Real-time shadows are rendered based on the model's geometry during surface-detection experiences
-   On iOS, 3D models are required to be in the USDZ format

**Videos**

-   The ImagineAR SDK is capable of rendering video assets to a 2D plane in the 3D AR scene
-   Video assets support green and blue chromakeying, with an adjustable threshold, this can provide a strong immersive experience for real world objects without creating 3D models
-   Videos are required to be in MP4 format

**Images**

-   Similar to video assets, basic image assets can also be rendered to a 2D plane and drawn in the 3D AR scene
-   Image assets can be provided in PNG or JPG format

Assets can be manipulated dynamically using metadata defined in the ImagineAR Cloud dashboard, allowing control over scale, offset positioning and rotation.

## Download Location Markers
If you are integrating AR experiences that will trigger based on user location, the recommended user experience will be to interact with a map view, displaying the locations of nearby AR experiences. When a user is within the specified distance from an AR experience, they should be able to launch the experience by tapping on the marker within the map.

Using the IAR SDK, an array of markers can be returned based off a radius around the users Latitude and Longitude

```tsx

import { downloadOnDemandMarkers, IARMarker } from 'react-native-iar-sdk';

const markers: IARLocationMarker[] = await getLocationMarkers(
  longitude,
  latitude,
  radius
);

```

The markers are returned in an array of type `IARLocationMarker` which is defined as:

```tsx
{
  id:  string;
  name:  string;
  image:  string;
  latitude:  number;
  longitude:  number;
  distance:  number;
  radius:  number;
}
```

## Download Rewards
Users can receive Rewards for viewing markers, using the IAR SDK, an array of rewards a user has previously been rewarded can be returned and shown to the users.

```tsx

import { getUserRewards, IARReward } from 'react-native-iar-sdk';

const userRewards: IARReward[] = await getUserRewards();

```

The rewards are returned in an array of type `IARReward` which is defined as:

```tsx

{
  id: string;
  name: string;
  image: string;
  rewardReasonType: string;
  type: string;
  actionButtonEnabled: boolean;
  actionButtonText: string;
  actionButtonUrl: string;
  generalPromoCode: string;
  generalPromoCodeOptionalText: string;
}

```

## Displaying the AR Experience
Both OnDemand and Location markers are OnDemand experiences. Following these instructions will launch a full-screen camera view to allow the user to place the AR asset on a surface and interact with it in the AR space via the camera.

```tsx

import { SurfaceView, IARPlaceButtonConfig } from 'react-native-iar-sdk';

const SurfaceViewScreen = () => {

  // Setup event listeners for SurafaceView Events
  const onProgressChange = (progress: number) => {
  	console.log('onProgressChange - progress: ', progress);
  };
  const onAssetAnchoredChange = (isAnchored: boolean)  =>  {
  	console.log('onAssetAnchoredChange - isAnchored: ', isAnchored);
  };  
  const onSurfaceDetected = (isSurfaceDetected: boolean) => {
  	console.log('onSurfaceDetected - isSurfaceDetected: ', isSurfaceDetected);
  };  
  const onRewardsAwarded = (rewards:  string[]) => {
  	console.log('onRewardsAwarded - rewards: ', rewards);
  };  
  
  // Setup a configuration for the placement button
  const placeButtonConfig: IARPlaceButtonConfig = {
    borderWidth: 5,
    borderRadius: 10,
    textColor: '#FFFFFF',
    backgroundColor: '#000000',
    borderColor: '#FFFFFF',
    width: 100,
    height: 50,
    fontSize: 14,
    fontWeight: 'bold',
    anchoredText: 'Move',
    unAnchoredText: 'Place',
  };  
  // Return the SurfaceView Component
  return (
    <SurfaceView
      markerId={route.params?.markerId}
      onDownloadProgressChange={(progress) => onProgressChange(progress)}
      isSurfaceDetected={(isSurfaceDetected) => onSurfaceDetected(isSurfaceDetected)}
      isAssetAnchored={(isAssetAnchored) => onAssetAnchoredChange(isAssetAnchored)}
      rewardsAwarded={(rewards) => onRewardsAwarded(rewards)}
      placeButtonConfig={placeButtonConfig}
    />
  );
};
export default SurfaceViewScreen;

```

The `SurfaceView` component is used to show the AR Experience. It will launch in a full screen view on android, and a full screen modal on iOS. It contains a configurable button that is used to place/move the 3D asset in camera space.

The SurfaceView component accepts a number of props.

`markerId` (required) 
This is the id of the marker that is to be downloaded and shown to the user

`onDownloadProgressChange` 
This prop returns a `number` between `0` and `1` representing the download progress of the marker. It can be used to indicate to your users that the download is taking place.

`isSurfaceDetected` 
This prop returns a `boolean` to indicate if a surface has been detected.

`isAssetAnchored` 
This prop returns a `boolean` to indicate if the asset has been anchored in camera space.

`rewardsAwarded` 
This prop returns an `array` of type `IARReward` that contains any rewards the user has received for interacting with the marker.  Marker rewards are rewarded by the SDK when the `markerId` is queried for the first time by a user, so if the user visits the marker again, the reward will not be rewarded a second time.

# Ready to Integrate?

The ImagineAR SDK is a paid service with flexible pricing, access to the ImagineAR Cloud dashboard is limited to our customers.

Read-only sample content is provided for test integrations to help ensure ImagineAR is the right tool for your application. If you're ready to test the integration into your mobile app, head to our  [integration documentation](https://docs.imaginear.com/docs/v1.5/ios/integration#integrate-the-sdk-into-your-application)  for all the details.

Have questions about integration or are ready to discuss pricing? Don't hesitate to reach out to us at  [info@imaginear.com](https://github.com/ImagineAR/IAR-SDK-Sample-iOS/blob/main/info@imaginear.com).

# License

The contents of this repository are licensed under the  [Apache License, version 2.0.](https://www.apache.org/licenses/LICENSE-2.0)  The use of Imagine AR SDK is governed by the  [Terms of Service](https://imaginear.com/terms-conditions)  for ImagineAR.