import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  ViewStyle,
  NativeEventEmitter,
  NativeModules,
  Platform,
  Text,
  StyleSheet,
  TouchableOpacity,
  Modal,
  PixelRatio,
} from 'react-native';
import type { IPlaceButtonConfig } from 'react-native-iar-sdk';
import SurfaceComponent from './SurfaceComponent';

export const SurfaceView = ({
  style,
  markerId,
  placeButtonConfig = {},
  onDownloadProgressChange,
  isSurfaceDetected,
  isAssetAnchored,
  rewardsAwarded,
}: {
  style: ViewStyle;
  markerId: string;
  placeButtonConfig?: IARPlaceButtonConfig;
  onDownloadProgressChange: (arg0: number) => void;
  isSurfaceDetected: (arg0: boolean) => void;
  isAssetAnchored: (arg0: boolean) => void;
  rewardsAwarded: (arg0: string[]) => void;
}) => {
  const [placeText, setPlaceText] = useState('Place');
  const [moveText, setMoveText] = useState('Move');
  const [placeButtonText, setPlaceButtonText] = useState(placeText);
  const [isAnchored, setIsAnchored] = useState(false);
  const [surfaceDetected, setSurfaceDetected] = useState(false);
  const [markerAssetsDownloaded, setMarkerAssetsDownloaded] = useState(false);
  const [isIosModalVisible, setIosIsModalVisible] = useState(false);

  const surfaceViewRef = useRef(null);

  const eventEmitter = new NativeEventEmitter(
    Platform.OS === 'ios' ? NativeModules.IAREventEmitter : NativeModules.IARSdk
  );

  // Setup Component Init
  useEffect(() => {
    isAssetAnchored(false); // Set asset Anchored to false
    // Listen for markerDownloadProgress
    const downloadProgressListener = eventEmitter.addListener(
      'markerDownloadProgress',
      (event) => {
        onDownloadProgressChange(event.progress / 100); // make progress out of 1 instead of 100
        if (event.progress === 100) {
          setIosIsModalVisible(true);
          setMarkerAssetsDownloaded(true);
        }
      }
    );
    // Listen for surfaceDetected
    const surfaceDetectedListener = eventEmitter.addListener(
      'surfaceDetected',
      (event) => {
        setSurfaceDetected(event.isSurfaceDetected);
        isSurfaceDetected(event.isSurfaceDetected);
      }
    );
    // Listen for isAssetAnchored
    const isAssetAnchoredListener = eventEmitter.addListener(
      'isAssetAnchored',
      (event) => {
        isAssetAnchored(event.isAssetAnchored);
        setIsAnchored(event.isAssetAnchored);
      }
    );
    // Listen for rewardsAwarded
    const rewardsAwardedListener = eventEmitter.addListener(
      'rewardsAwarded',
      (event) => {
        rewardsAwarded(event.rewards);
      }
    );

    // Remove listeners when component is discarded
    return () => {
      downloadProgressListener.remove();
      surfaceDetectedListener.remove();
      isAssetAnchoredListener.remove();
      rewardsAwardedListener.remove();
    };

    // Only run on first render
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Set Android Pixel Ratio
  const setPixelRatio = (nativeSize: number) => {
    if (Platform.OS === 'android') {
      const pixelSize = Math.floor(
        PixelRatio.roundToNearestPixel(nativeSize * PixelRatio.get())
      );
      return pixelSize;
    }
    return nativeSize;
  };

  // Transform pixel sizes for android
  const androidPlaceButtonConfig = (btnConfig: IPlaceButtonConfig) => {
    let androidBtnConfig = {
      ...btnConfig,
    };
    if (btnConfig.height) {
      androidBtnConfig = {
        ...androidBtnConfig,
        height: setPixelRatio(btnConfig.height),
      };
    }
    if (btnConfig.width) {
      androidBtnConfig = {
        ...androidBtnConfig,
        width: setPixelRatio(btnConfig.width),
      };
    }
    if (btnConfig.borderWidth) {
      androidBtnConfig = {
        ...androidBtnConfig,
        borderWidth: setPixelRatio(btnConfig.borderWidth),
      };
    }
    if (btnConfig.borderRadius) {
      androidBtnConfig = {
        ...androidBtnConfig,
        borderRadius: setPixelRatio(btnConfig.borderRadius),
      };
    }
    return androidBtnConfig;
  };

  // Setup Button Style if config is provided
  const buttonStyleOverride = StyleSheet.create({
    buttonContainer: {
      ...styles.buttonContainer,
    },
    button: {
      ...styles.button,
      width: placeButtonConfig?.width
        ? placeButtonConfig?.width
        : styles.button.width,
      height: placeButtonConfig?.height
        ? placeButtonConfig?.height
        : styles.button.height,
      backgroundColor: placeButtonConfig?.backgroundColor
        ? placeButtonConfig?.backgroundColor
        : styles.button.backgroundColor,
      borderRadius: placeButtonConfig?.borderRadius
        ? placeButtonConfig?.borderRadius
        : styles.button.borderRadius,
      borderWidth: placeButtonConfig?.borderWidth
        ? placeButtonConfig?.borderWidth
        : styles.button.borderWidth,
      borderColor: placeButtonConfig?.borderColor
        ? placeButtonConfig?.borderColor
        : styles.button.borderColor,
    },
    buttonText: {
      ...styles.buttonText,
      color: placeButtonConfig?.textColor
        ? placeButtonConfig?.textColor
        : styles.buttonText.color,
      fontSize: placeButtonConfig?.fontSize
        ? placeButtonConfig?.fontSize
        : styles.buttonText.fontSize,
      fontWeight: placeButtonConfig?.fontWeight
        ? placeButtonConfig?.fontWeight
        : styles.buttonText.fontWeight,
    },
    placeTextContainer: {
      ...styles.placeTextContainer,
      height: placeButtonConfig?.height
        ? placeButtonConfig?.height
        : styles.button.height,
    },
    closeButton: {
      ...styles.closeButton,
      backgroundColor: placeButtonConfig?.backgroundColor
        ? placeButtonConfig?.backgroundColor
        : styles.button.backgroundColor,
      borderRadius: placeButtonConfig?.borderRadius
        ? placeButtonConfig?.borderRadius
        : styles.button.borderRadius,
      borderWidth: placeButtonConfig?.borderWidth
        ? placeButtonConfig?.borderWidth
        : styles.button.borderWidth,
      borderColor: placeButtonConfig?.borderColor
        ? placeButtonConfig?.borderColor
        : styles.button.borderColor,
    },
  });

  // Change the text on load if config is provided
  useEffect(() => {
    if (placeButtonConfig?.anchoredText) {
      setMoveText(placeButtonConfig.anchoredText);
    }
    if (placeButtonConfig?.unAnchoredText) {
      setPlaceText(placeButtonConfig.unAnchoredText);
    }
  }, [placeButtonConfig?.anchoredText, placeButtonConfig?.unAnchoredText]);

  // Change Button Text
  useEffect(() => {
    let buttonTitle = placeText;
    if (isAnchored) {
      buttonTitle = moveText;
    }
    setPlaceButtonText(buttonTitle);
  }, [isAnchored, moveText, placeText]);

  // Button Presses/Interactions
  const onPlaceButtonPress = () => {
    setIsAnchored(!isAnchored);
  };

  const onCloseModal = () => {
    setIosIsModalVisible(false);
  };

  return (
    <View style={style ? style : styles.container}>
      {Platform.OS === 'ios' ? (
        <Modal
          visible={isIosModalVisible}
          presentationStyle="fullScreen" // or "fullScreen" for actual fullscreen - would need a close button
          animationType="slide"
          onRequestClose={onCloseModal}
        >
          <SurfaceComponent
            style={styles.iosNativeSurfaceView}
            markerId={markerId}
            assetAnchored={isAnchored.toString()}
            ref={surfaceViewRef}
          />
          {isAnchored ? (
            <View style={buttonStyleOverride.buttonContainer}>
              <TouchableOpacity
                style={buttonStyleOverride.button}
                onPress={onPlaceButtonPress}
                activeOpacity={1}
              >
                <Text style={buttonStyleOverride.buttonText}>
                  {placeButtonText}
                </Text>
              </TouchableOpacity>
            </View>
          ) : (
            <View style={buttonStyleOverride.buttonContainer}>
              {surfaceDetected ? (
                <View style={buttonStyleOverride.placeTextContainer}>
                  <Text style={buttonStyleOverride.buttonText}>
                    {placeButtonText}
                  </Text>
                </View>
              ) : null}
            </View>
          )}
          <View style={styles.closeButtonContainer}>
            <TouchableOpacity
              style={buttonStyleOverride.closeButton}
              onPress={onCloseModal}
            >
              <Text style={buttonStyleOverride.buttonText}>X</Text>
            </TouchableOpacity>
          </View>
        </Modal>
      ) : (
        <View>
          <SurfaceComponent
            style={{ flex: 1 }}
            markerId={markerId}
            ref={surfaceViewRef}
            placeButtonConfig={androidPlaceButtonConfig(placeButtonConfig)}
          />
        </View>
      )}
      {!markerAssetsDownloaded ? (
        <SurfaceComponent
          style={styles.hiddenSurfaceComponent}
          markerId={markerId}
          assetAnchored={isAnchored.toString()}
          ref={surfaceViewRef}
        />
      ) : null}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  buttonContainer: {
    position: 'absolute',
    width: '100%',
    bottom: 20,
    alignItems: 'center',
    justifyContent: 'flex-end',
  },
  placeTextContainer: {
    width: 300,
    height: 50,
    padding: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  button: {
    width: 100,
    height: 50,
    backgroundColor: '#000000',
    padding: 10,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 0,
    borderWidth: 0,
    borderColor: '#000000',
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  iosNativeSurfaceView: {
    flex: 1,
  },
  closeButtonContainer: {
    position: 'absolute',
    bottom: 20,
    right: 20,
    alignItems: 'center',
    justifyContent: 'flex-end',
  },
  closeButton: {
    width: 50,
    height: 50,
    backgroundColor: '#ff0000',
    borderRadius: 50,
    alignItems: 'center',
    justifyContent: 'center',
  },
  closeButtonText: {},
  hiddenSurfaceComponent: {
    width: 0,
    height: 0,
  },
});
