//
//  NICInfoSummary.h
//  NICInfo
//
//  Class for getting network interfaces address information instantly.
//  Refer to showNICInfo method of ViewController for USAGE.
//
//
//  AUTHOR          : kenial (keniallee@gmail.com)
//

#import <Foundation/Foundation.h>
#import "NICInfo.h"

@interface NICInfoSummary : NSObject {
    NSArray*    nicInfos;
}

@property (readonly) NSArray* nicInfos;

+ (NICInfoSummary *)infoSummary;
+ (NICInfoSummary *)refreshInfoSummary;

// Let me have all NIC information on this device!
- (NICInfo *)findNICInfo:(NSString*)interface_name;

// iPhone's NIC :
//  pdp_ip0 : 3G
//  en0 : wifi
//  en2 : bluetooth
//  bridge0 : personal hotspot

// macbook air's NIC (it varies on devices) :
//  en0 : wifi
//  en1 : iphone USB
//  en2 : bluetooth


- (bool)isWifiConnected;
- (bool)isWifiConnectedToNAT;
- (bool)isBluetoothConnected;
- (bool)isPersonalHotspotActivated;
- (bool)is3GConnected;

- (NSArray *)broadcastIPs;

@end
