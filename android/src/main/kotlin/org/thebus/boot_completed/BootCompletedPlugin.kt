package org.thebus.boot_completed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.flutter.plugin.common.PluginRegistry
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments
import java.lang.ref.SoftReference

//based off of what android_alarm_manager does
class BootCompletedPlugin: BroadcastReceiver(){

  override fun onReceive(p0: Context?, p1: Intent?) {
      doDartCallback(p0!!.applicationContext)
  }

  private fun doDartCallback(callbackContext: Context){

    //this call is cargo cult to me
    //not sure what terrible things could happen if we don't block
    //"until initialization of the native system is completed"
    //but it was there
    //so, eh.
    FlutterMain.ensureInitializationComplete(callbackContext, null)

    val mAppBundlePath = FlutterMain.findAppBundlePath(callbackContext)

    sBackgroundFlutterViewRef = SoftReference(FlutterNativeView(callbackContext, true))

    val args = FlutterRunArguments()

    args.bundlePath = mAppBundlePath

    //being able to hardcode the method name/library path
    //isn't documented anywhere, I think
    //and is also probably extremely fragile

    //if it stops working, should probably just go back to the convoluted method
    //of PluginUtilities.getCallbackHandle from the dart side
    //save that somewhere accessible by the android side
    //(probably via MethodChannel)
    //and then read it back in later
    //and use FlutterCallbackInformation.lookupCallbackInformation
    //to get the .callbackName and .callbackLibraryPath
    args.entrypoint = "_executeOnBootCompleted"
    args.libraryPath = "package:boot_completed/boot_completed.dart"

    getBGFlutterView().runFromBundle(args)

    sPluginRegistrantCallback!!.registerWith(getBGFlutterView().pluginRegistry)
  }

  companion object {

    var sBackgroundFlutterViewRef: SoftReference<FlutterNativeView>? = null
    fun getBGFlutterView() = sBackgroundFlutterViewRef!!.get()!!

    //allows android application to register with the flutter plugin registry
    var sPluginRegistrantCallback: PluginRegistry.PluginRegistrantCallback? = null

    //FlutterApplication subclass needs to call this
    //in order to let the plugin call registerWith
    //which should in turn call GeneratedPluginRegistrant.registerWith
    //which apparently does some voodoo magic that lets this whole thing work
    //despite the registerWith of the plugin itself doing exactly nothing
    fun setPluginRegistrantCallback(theCallback: PluginRegistry.PluginRegistrantCallback) {
      sPluginRegistrantCallback = theCallback
    }

    //GeneratedPluginRegistrant will call this method
    @JvmStatic
    fun registerWith(registrar: PluginRegistry.Registrar) {
      //if the FlutterApplication override does not call GeneratedPluginRegistrant.registerWith
      //this plugin will not work

      //however, if GeneratedPluginRegistrant.registerWith is called,
      //GeneratedPluginRegistrant will call this function

      //so it needs to be here, despite doing literally nothing
    }
  }
}