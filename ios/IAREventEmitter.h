//
//  IAREventEmitter.h
//  react-native-iar-sdk
//
//  Created by Wes Goodhoofd on 2023-02-27.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import "IAR_Core_SDK/IAR_Core_SDK-Swift.h"

NS_ASSUME_NONNULL_BEGIN

@interface IAREventEmitter : RCTEventEmitter <RCTBridgeModule>

- (void)surfaceDetected;
- (void)markerDownloadProgress:(CGFloat)progress;
- (void)isAssetAnchored:(BOOL)isAnchored;
- (void)rewardsAwarded:(NSArray<Reward *> *)rewards;
- (void)sendEventIfListeners:(NSString *)name data:(id)data;

@end

NS_ASSUME_NONNULL_END
