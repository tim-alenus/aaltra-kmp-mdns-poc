#import <Foundation/Foundation.h>
#import <Network/Network.h>

@interface NWBrowserBridge : NSObject

- (instancetype)init;

- (void)startBrowsingWithServiceType:(NSString *)serviceType
                              domain:(NSString * _Nullable)domain
                      onServiceFound:(void (^)(NSString *, NSString *, NSString *))onServiceFound
                    onServiceRemoved:(void (^)(NSString *))onServiceRemoved
                             onError:(void (^)(NSString *))onError;

- (void)stop;

- (void)resolveServiceWithName:(NSString *)name
                          type:(NSString *)type
                        domain:(NSString *)domain
                    onResolved:(void (^)(NSString *, NSArray<NSString *> *, NSInteger, NSString *, NSDictionary<NSString *, NSData *> *))onResolved
                       onError:(void (^)(NSString *))onError;

@end

