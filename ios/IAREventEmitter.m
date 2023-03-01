//
//  IAREventEmitter.m
//  react-native-iar-sdk
//
//  Created by Wes Goodhoofd on 2023-02-27.
//

#import "IAREventEmitter.h"

@implementation IAREventEmitter
{
    bool hasListeners;
}
RCT_EXPORT_MODULE();

+ (id)allocWithZone:(NSZone *)zone {
    static IAREventEmitter *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}

-(NSArray<NSString*>*)supportedEvents {
    return @[@"markerDownloadProgress", @"isAssetAnchored", @"surfaceDetected", @"rewardsAwarded"];
}

// Will be called when this module's first listener is added.
-(void)startObserving {
    hasListeners = YES;
}

// Will be called when this module's last listener is removed, or on dealloc.
-(void)stopObserving {
    hasListeners = NO;
}

-(void)sendEventIfListeners:(NSString*)name data:(id)data {
    if(hasListeners) {
        [self sendEventWithName:name body:data];
    }
}

-(void)surfaceDetected {
    [self sendEventIfListeners:@"surfaceDetected" data:@{@"isSurfaceDetected": @(YES)}];
}

-(void)markerDownloadProgress:(CGFloat)progress {
    NSLog(@"IAREventEmitter - Progress = %.2f", progress);
    [self sendEventIfListeners:@"markerDownloadProgress" data:@{@"progress": [NSNumber numberWithInt:(progress * 100.0)]}];
}

-(void)isAssetAnchored:(BOOL)isAnchored {
    [self sendEventIfListeners:@"isAssetAnchored" data:@{@"isAssetAnchored": @(isAnchored)}];
}

-(void)rewardsAwarded:(NSArray<Reward*>*)rewards {
    NSMutableArray<NSString*>* idArray = [[NSMutableArray alloc] init];
    for (Reward* reward in rewards) {
        [idArray addObject:reward.rewardId];
    }
    
    [self sendEventIfListeners:@"rewardsAwarded" data:@{@"rewardsAwarded": idArray}];
}
@end
