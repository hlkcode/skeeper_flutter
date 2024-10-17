#import "SkeeperFlutterPlugin.h"
#if __has_include(<skeeper_flutter/skeeper_flutter-Swift.h>)
#import <skeeper_flutter/skeeper_flutter-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "skeeper_flutter-Swift.h"
#endif

@implementation SkeeperFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftSkeeperFlutterPlugin registerWithRegistrar:registrar];
}
@end
