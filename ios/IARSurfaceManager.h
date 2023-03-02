#import <React/RCTViewManager.h>
#import "IAR_Surface_SDK/IAR_Surface_SDK.h"
#import "IAREventEmitter.h"

@interface IARSurfaceViewManager : RCTViewManager <IARSurfaceViewDelegate>
{
    IARSurfaceView *surfaceView;
    IAREventEmitter *_eventEmitter;
}

@end
