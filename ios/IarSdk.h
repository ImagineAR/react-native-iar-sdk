#import "IAR_Core_SDK/IARNetworkManager.h"
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNIarSdkSpec.h"

@interface IarSdk : NSObject <NativeIarSdkSpec>
#else
#import <React/RCTBridgeModule.h>

@interface IarSdk : NSObject <RCTBridgeModule>
#endif


@end
