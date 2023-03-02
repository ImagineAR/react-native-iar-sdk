import { NativeModules, Platform } from 'react-native';
export { SurfaceView } from './SurfaceView';

const LINKING_ERROR =
  `The package 'react-native-iar-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

export interface IARMarker {
  id: string;
  name: string;
  image: string;
}

export interface IARLocationMarker {
  id: string;
  name: string;
  image: string;
  latitude: number;
  longitude: number;
  distance: number;
  radius: number;
}

export interface IARReward {
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

export interface IARPlaceButtonConfig {
  borderWidth?: number;
  borderRadius?: number;
  textColor?: string;
  fontSize?: number;
  fontWeight?: 'normal' | 'bold';
  backgroundColor?: string;
  borderColor?: string;
  width?: number;
  height?: number;
  anchoredText?: string;
  unAnchoredText?: string;
}

const IarSdk = NativeModules.IarSdk
  ? NativeModules.IarSdk
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function initialize(a: string): Promise<string> {
  return IarSdk.initialize(a);
}

export function downloadOnDemandMarkers(): Promise<IARMarker[]> {
  return IarSdk.downloadOnDemandMarkers();
}

export function iarLicense(): Promise<string> {
  return IarSdk.iarLicense();
}

export function isLicenseValid(): Promise<boolean> {
  return IarSdk.iarLicenseIsValid();
}

export function createExternalUserId(userID: string): Promise<string> {
  return IarSdk.createExternalUserId(userID);
}
export function setExternalUserId(userID: string): Promise<string> {
  return IarSdk.setExternalUserId(userID);
}

export function getExternalUserId(): Promise<string> {
  return IarSdk.getExternalUserId();
}

export function retrieveHunts(): Promise<[object]> {
  return IarSdk.retrieveHunts();
}

export function getUserRewards(): Promise<IARReward[]> {
  return IarSdk.getUserRewards();
}
export function getLocationMarkers(
  latitude: number,
  longitude: number,
  radius: number
): Promise<IARLocationMarker[]> {
  return IarSdk.getLocationMarkers(latitude, longitude, radius);
}
