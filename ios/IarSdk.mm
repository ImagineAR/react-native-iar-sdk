#import "IarSdk.h"
#import "IAR_Core_SDK/IARLicenseManager.h"
#import "IAR_Core_SDK/IAR_Core_SDK-Swift.h"
#import "IAR_Surface_SDK/IAR_Surface_SDK.h"
#import "IAR_Core_SDK/IARNetworkManager.h"


@implementation IarSdk
RCT_EXPORT_MODULE()

RCT_REMAP_METHOD(initialize,
                license: (NSString *)license
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    [[IARLicenseManager shared] validateLicense:license serverType:CloudServerUS onComplete:^(NSError * _Nullable error) {
        if (error) {
            reject(@"invalid_license", @"License key not valid", error);
        } else {
            resolve(@"License successfully validated");
        }
    }];
}

// Return License used
RCT_EXPORT_METHOD(iarLicense:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *license = [[IARLicenseManager shared ] iarLicense];
    resolve(license);
}

// Return if Licence is valid
RCT_EXPORT_METHOD(iarLicenseIsValid:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    bool isLicenseValid = [IARLicenseManager shared ].iarLicenseIsValid;
    resolve([NSNumber numberWithBool: isLicenseValid]);
}

// Set User ID
RCT_REMAP_METHOD(createExternalUserId,
                userId: (NSString *)userId
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    [[IARNetworkManager shared] createExternalUserId:(userId) completeCallback:^(NSString * _Nullable externalUserId, NSError * _Nullable error) {
        if (error) {
            reject(@"user_creation_failed", @"Failed to create UserID: ", error);
        } else {
            NSString *prependString = @"UserId Set: ";
            NSString *callbackString = [prependString stringByAppendingString:externalUserId];
            
            // Set External UserID on the Network Manager
            [[IARNetworkManager shared] setExternalUserId:externalUserId];
            resolve(callbackString);
        }
    }];

};

RCT_REMAP_METHOD(setExternalUserId, userId: (NSString *)userId)
{
    [[IARNetworkManager shared] setExternalUserId:userId];
};

// Return external userId
RCT_EXPORT_METHOD(getExternalUserId:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSString *userId = [IARNetworkManager shared ].externalUserId;
    resolve(userId);
}

- (NSString *)prettyPrintedJson:(id)jsonObject
{
    NSData *jsonData;

    if ([NSJSONSerialization isValidJSONObject:jsonObject]) {
        NSError *error;
        jsonData = [NSJSONSerialization dataWithJSONObject:jsonObject
                                                           options:NSJSONWritingPrettyPrinted
                                                             error:&error];

        if (error) {
            return nil;
        }
    } else {
        jsonData = jsonObject;
    }

    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

// fetch OnDemand Markers under the current OrgKey
RCT_REMAP_METHOD(downloadOnDemandMarkers,
                 downloadMarkersWithResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    
    [[IARNetworkManager shared] downloadOnDemandMarkersWithCallback:^(NSArray<Marker *> * _Nullable markers, NSError * _Nullable error) {
        if (error) {
            reject(@"OnDemand Markers Error", @"Markers Not found", error);
        } else {
            NSMutableArray *markerArray = [NSMutableArray new];
            
            for (Marker *marker in markers) {
                NSMutableDictionary *markerDictionary =  [[NSMutableDictionary alloc] init];
                
                [markerDictionary setObject:marker.markerId forKey:@"id"];
                [markerDictionary setObject:marker.name forKey:@"name"];
                
                if(marker.previewImageUrl != nil) {
                    [markerDictionary setObject:marker.previewImageUrl forKey:@"image"];
                } else {
                    [markerDictionary setObject:@"" forKey:@"image"];
                }
                
                [markerArray addObject:markerDictionary];
            }
            
            resolve(markerArray);
        }
    }];
}

RCT_REMAP_METHOD(getLocationMarkers,
                latitude: (nonnull NSNumber *)latitude
                longitude: (nonnull NSNumber *)longitude
                radius: (nonnull NSNumber *)radius
                 getLocationMarkersResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    double latitudeDouble = [latitude doubleValue];
    double longitudeDouble = [longitude doubleValue];
    double radiusDouble = [radius doubleValue];
    [[IARNetworkManager shared] downloadLocationMarkers:latitudeDouble longitude:longitudeDouble radius:radiusDouble completeCallback:^(NSArray<Marker *> * _Nullable markers, NSError * _Nullable error) {
        if (error) {
            reject(@"Location Markers Error", @"Markers Not found", error);
        } else {
            NSMutableArray *markerArray = [NSMutableArray new];
            
            for (Marker *marker in markers) {
                NSMutableDictionary *markerDictionary =  [[NSMutableDictionary alloc] init];
                
                [markerDictionary setObject:marker.markerId forKey:@"id"];
                [markerDictionary setObject:marker.name forKey:@"name"];
                
                
                if(marker.previewImageUrl != nil) {
                    [markerDictionary setObject:marker.previewImageUrl forKey:@"image"];
                } else {
                    [markerDictionary setObject:@"" forKey:@"image"];
                }
                
                [markerArray addObject:markerDictionary];
            }
            
            resolve(markerArray);
        }
    }];
}

RCT_REMAP_METHOD(getUserRewards,
                 getUserRewardsWithResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    
    [[IARNetworkManager shared] downloadUserRewards:^(NSArray *rewards, NSError *error) {
        if (error) {
            reject(@"Rewards Error", @"error", error);
        } else {
            NSMutableArray *rewardArray = [NSMutableArray new];
            
            for (Reward *reward in rewards) {
                NSMutableDictionary *rewardDictionary =  [[NSMutableDictionary alloc] init];
                
                NSNumber *actionButtonEnabledNumber = [NSNumber numberWithBool:reward.actionButtonEnabled];
                
                
                [rewardDictionary setObject:reward.rewardId forKey:@"id"];
                [rewardDictionary setObject:reward.rewardName forKey:@"name"];
                [rewardDictionary setObject:reward.file.url forKey:@"image"];
                [rewardDictionary setObject:reward.reasonType forKey:@"rewardReasonType"];
                [rewardDictionary setObject:reward.reasonType forKey:@"type"];
                [rewardDictionary setObject:actionButtonEnabledNumber forKey:@"actionButtonEnabled"];
                
                if(reward.actionButtonText != nil) {
                    [rewardDictionary setObject:reward.actionButtonText forKey:@"actionButtonText"];
                }
                
                if(reward.actionButtonUrl != nil) {
                    [rewardDictionary setObject:reward.actionButtonUrl forKey:@"actionButtonUrl"];
                }
                if(reward.generalPromoCode != nil) {
                    if(reward.generalPromoCode.promoCode != nil) {
                        [rewardDictionary setObject:reward.generalPromoCode.promoCode forKey:@"generalPromoCode"];
                    }
                    
                    if(reward.generalPromoCode.optionalText != nil) {
                        [rewardDictionary setObject:reward.generalPromoCode.optionalText forKey:@"generaPromoCodeOptionalText"];
                    }
                }

                [rewardArray addObject:rewardDictionary];
            }
            
            resolve(rewardArray);
        }
    }];
}

RCT_REMAP_METHOD(retrieveHunts,
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject) {
    [[IARNetworkManager shared] retrieveHunts:^(NSArray<Hunt *> * _Nullable hunt, NSError * _Nullable error) {
            if (error) {
                reject(@"retrieve_hunts_error", @"Unable to retrieve hunts", error);
            } else if (hunt) {
                resolve(hunt);
            } else {
                reject(@"retrieve_hunts_error", @"Unknown hunt error", error);
            }
    }];
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeIarSdkSpecJSI>(params);
}
#endif

@end
