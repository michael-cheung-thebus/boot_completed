#import "BootCompletedPlugin.h"
#if __has_include(<boot_completed/boot_completed-Swift.h>)
#import <boot_completed/boot_completed-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "boot_completed-Swift.h"
#endif

@implementation BootCompletedPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBootCompletedPlugin registerWithRegistrar:registrar];
}
@end
