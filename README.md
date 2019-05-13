# boot_completed

Flutter plugin to execute dart code on boot completed.

## Getting Started

A bunch of extra things need to be done to use this plugin:

1.  In your dart code,
    call boot_completed.setBootCompletedFunction(functionName)

    **See main() in example/lib/main.dart**


2.  On the android side,
    register org.thebus.boot_completed.BootCompletedPlugin as a broadcast receiver

    **See example/android/app/src/main/AndroidManifest.xml**


3.  On the android side, your application needs to subclass FlutterApplication,
    and implement PluginRegistry.PluginRegistrantCallback

    org.thebus.boot_completed.BootCompletedPlugin.setPluginRegistrantCallback(this)
    needs to be called,

    and PluginRegistrantCallback.registerWith should call
    GeneratedPluginRegistrant.registerWith()

    **see example/android/app/src/main/kotlin/org/thebus/boot_completed_example/ExampleApplication.kt**

    !!Don't forget to tell android to use your application subclass instead of FlutterApplication
    !!by modifying your AndroidManifest.xml
    !!**See the application section in example/android/app/src/main/AndroidManifest.xml**
