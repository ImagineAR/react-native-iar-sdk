#import "IARSurfaceManager.h"
#import "IAR_Core_SDK/IAR_Core_SDK.h"
#import <React/RCTViewManager.h>
#import <AVFoundation/AVCaptureDevice.h>


@implementation IARSurfaceViewManager

RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (instancetype)init
{
  if (self = [super init]) {
    _eventEmitter = [[IAREventEmitter alloc] init];
  }
  return self;
}

- (UIView *)view
{
    surfaceView = [[IARSurfaceView alloc] init];
    surfaceView.delegate = self;

    
    [AVCaptureDevice requestAccessForMediaType: AVMediaTypeVideo completionHandler:^(BOOL granted) {
        if (granted) {
            dispatch_async(dispatch_get_main_queue(), ^{
                NSLog(@"IARSurfaceManager - camera permission granted");
                [self->surfaceView load];
                
                double delayInSeconds = 2;
                dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
                dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                    NSLog(@"IARSurfaceManager - start surface view");
                    [self->surfaceView start];
                });
            });
        }
    }];
    return surfaceView;
}

RCT_CUSTOM_VIEW_PROPERTY(markerId, NSString, IARSurfaceView) {
    [[IARNetworkManager shared] downloadMarker:json completeCallback:^(Marker * _Nullable marker, NSError * _Nullable error) {
        // do something with the marker
        if (marker == nil && error != nil) {
            NSLog(@"IARSurfaceManager - downloadMarker marker error %@", error.localizedDescription);
            return;
        }
        [[IARNetworkManager shared] downloadAssetsAndRewardsFromMarker:marker completeCallback:^(Marker * _Nullable marker, NSError * _Nullable error) {
            if (error != nil) {
                NSLog(@"IARSurfaceManager - downloadAssetsAndRewardsFromMarker asset error %@", error.localizedDescription);
            } else {
                NSLog(@"IARSurfaceManager - downloadAssetsAndRewardsFromMarker Download Complete");
                [self->_eventEmitter markerDownloadProgress: 1.0];
                self->surfaceView.marker = marker;
            }
        } progressCallback:^(NSNumber * _Nonnull progress) {
            NSLog(@"IARSurfaceManager - downloadAssetsAndRewardsFromMarker progressCallback - progress %@", progress);
        }];
    } progressCallback:^(NSNumber * _Nonnull progress) {
        NSLog(@"IARSurfaceManager - downloadMarker progressCallback - progress %@", progress);
        [self->_eventEmitter markerDownloadProgress: progress.floatValue];
    }];
}
RCT_CUSTOM_VIEW_PROPERTY(assetAnchored, NSString, IARSurfaceView) {
    NSLog(@"IARSurfaceManager - assetAnchored: %@", json);
    if([json isEqualToString:@"false"]) {
        NSLog(@"IARSurfaceManager - moving asset");
        [surfaceView unanchorAsset];
    }
}

// Surface view delegate

-(void)surfaceViewAssetAnchored:(IARSurfaceView *)surfaceView {
    [_eventEmitter isAssetAnchored:true];
}
-(void)surfaceView:(IARSurfaceView *)surfaceView onError:(NSError *)error {
    NSLog(@"%@", error.localizedDescription);
}
-(void)surfaceViewSurfaceDetected:(IARSurfaceView *)surfaceView {
    [_eventEmitter surfaceDetected];
}
-(void)surfaceView:(IARSurfaceView *)surfaceView downloadProgress:(CGFloat)progress {
    NSLog(@"IARSurfaceManager - surface progress %.2f", progress);
}
-(BOOL)surfaceViewCanScaleAsset:(IARSurfaceView *)surfaceView {
    return true;
}
-(BOOL)surfaceViewOnlyShowAssetOnTap:(IARSurfaceView *)surfaceView {
    return false;
}
-(void)unanchorAsset:(IARSurfaceView *)surfaceView{};

@end
