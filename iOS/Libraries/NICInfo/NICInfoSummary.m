#import "NICInfoSummary.h"

@implementation NICInfoSummary

// performance of nic info when alloc/dealloc 1000 times
//  iPhone 4 : 1.525310s
//  iPod Touch 1G : 2.533612s
- (void)dealloc
{
    if(nicInfos != nil)
        [nicInfos release];
    [super dealloc];
}

static NICInfoSummary *_sharedInfoSummary;
+ (NICInfoSummary *)infoSummary
{
    if(nil == _sharedInfoSummary)
        _sharedInfoSummary = [[NICInfoSummary alloc] init];
    return _sharedInfoSummary;
}

+ (NICInfoSummary *)refreshInfoSummary
{
    @synchronized(_sharedInfoSummary)
    {
        if(nil != _sharedInfoSummary)
        {
            [_sharedInfoSummary release];
            _sharedInfoSummary = nil;
        }
    }
    return [NICInfoSummary infoSummary];
}

- (NSArray*)nicInfos
{
    if(nicInfos == nil)
        nicInfos = [[NICInfo nicInfos] retain];
    return nicInfos;
}

- (NICInfo*)findNICInfo:(NSString*)interface_name
{
    for(int i=0; i<self.nicInfos.count; i++)
    {
        NICInfo* nic_info = [self.nicInfos objectAtIndex:i];
        if([nic_info.interfaceName isEqualToString:interface_name])
            return nic_info;
    }
    return nil;
}

- (bool)isWifiConnected
{
    NICInfo* nic_info = nil;
    nic_info = [self findNICInfo:@"en0"];
    if(nic_info != nil)
    {
        if(nic_info.nicIPInfos.count > 0)
            return YES;
    }
    return NO;
}

- (bool)isWifiConnectedToNAT
{
    NICInfo* nic_info = nil;
    nic_info = [self findNICInfo:@"en0"];
    if(nic_info != nil)
    {
        for(int i=0; i<nic_info.nicIPInfos.count; i++)
        {
            NICIPInfo* ip_info = [nic_info.nicIPInfos objectAtIndex:i];
            NSRange range;
            range = [ip_info.ip rangeOfString:@"192.168."];
            if(range.location == 0)
                return YES;
            range = [ip_info.ip rangeOfString:@"10."];
            if(range.location == 0)
                return YES;
        }
    }
    return NO;
}

- (bool)isBluetoothConnected
{
    NICInfo* nic_info = nil;
    nic_info = [self findNICInfo:@"en2"];
    if(nic_info != nil)
    {
        if(nic_info.nicIPInfos.count > 0)
            return YES;
    }
    return NO;
}

- (bool)isPersonalHotspotActivated
{
    NICInfo* nic_info = nil;
    nic_info = [self findNICInfo:@"bridge0"];
    if(nic_info != nil)
    {
        if(nic_info.nicIPInfos.count > 0)
            return YES;
    }
    return NO;
}

- (bool)is3GConnected
{
    NICInfo* nic_info = nil;
    nic_info = [self findNICInfo:@"pdp_ip0"];
    if(nic_info != nil)
    {
        if(nic_info.nicIPInfos.count > 0)
            return YES;
    }
    return NO;
}

// return all broadcast ip, except 127.0.01
- (NSArray *)broadcastIPs
{
    NSMutableArray *array = [[[NSMutableArray alloc] init] autorelease];
    if(self.nicInfos != nil)
    {
        for(NICInfo *nic in self.nicInfos)
        {
            if(nic.nicIPInfos != nil)
            {
                for(NICIPInfo *ipInfo in nic.nicIPInfos)
                {
                    if(![ipInfo.ip isEqualToString:@"127.0.0.1"] &&
                       ![ipInfo.ip isEqualToString:ipInfo.broadcastIP])
                        [array addObject:ipInfo.broadcastIP];
                }
            }        
        }   
    }
    return array;
}
@end
