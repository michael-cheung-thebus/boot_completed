package org.thebus.boot_completed_example

import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugins.GeneratedPluginRegistrant
import org.thebus.boot_completed.BootCompletedPlugin

class ExampleApplication: FlutterApplication(), PluginRegistry.PluginRegistrantCallback{

    override fun onCreate() {
        super.onCreate()
        BootCompletedPlugin.setPluginRegistrantCallback(this)
    }

    override fun registerWith(p0: PluginRegistry?) {
        GeneratedPluginRegistrant.registerWith(p0)
    }
}