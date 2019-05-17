package org.thebus.boot_completed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.flutter.plugin.common.JSONMethodCodec
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments
import org.json.JSONArray
import java.lang.ClassCastException
import java.lang.ref.SoftReference

//based off of what android_alarm_manager does
class BootCompletedPlugin: BroadcastReceiver(), MethodChannel.MethodCallHandler{

  override fun onReceive(p0: Context?, p1: Intent?) {

    if(contextRef == null){
      contextRef = SoftReference(p0!!)
    }

    val dartEntryPoint = getDartEntryPoint()

    if(dartEntryPoint != null) {
      doDartCallback(p0!!.applicationContext, dartEntryPoint)
    }
  }

  private fun doDartCallback(callbackContext: Context, callbackHandle: Long){

    //this call is cargo cult to me
    //not sure exactly what it means to block
    //"until initialization of the native system is completed"
    FlutterMain.ensureInitializationComplete(callbackContext, null)

    val mAppBundlePath = FlutterMain.findAppBundlePath(callbackContext)

    val flutterCallback = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

    sBackgroundFlutterViewRef = SoftReference(FlutterNativeView(callbackContext, true))

    val args = FlutterRunArguments()

    args.bundlePath = mAppBundlePath
    args.entrypoint = flutterCallback.callbackName
    args.libraryPath = flutterCallback.callbackLibraryPath

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
    fun setPluginRegistrantCallback(theCallback: PluginRegistry.PluginRegistrantCallback){
      sPluginRegistrantCallback = theCallback
    }

    //GeneratedPluginRegistrant will call this method
    @JvmStatic
    fun registerWith(registrar: PluginRegistry.Registrar){

      //if the FlutterApplication override does not call GeneratedPluginRegistrant.registerWith
      //this plugin will not work

      //and when GeneratedPluginRegistrant.registerWith is called,
      //GeneratedPluginRegistrant will call this function

      if(contextRef == null){
        contextRef = SoftReference(registrar.context())
      }

      val dartEntryPointChannelName = "org.thebus.boot_completed.SaveDartEntryPoint"
      val dartEntryPointChannel = MethodChannel(registrar.messenger(),dartEntryPointChannelName,JSONMethodCodec.INSTANCE)

      val bcp = BootCompletedPlugin()

      dartEntryPointChannel.setMethodCallHandler(bcp)
    }

    private var contextRef: SoftReference<Context>? = null
    private fun getContext() = contextRef!!.get()!!

    private const val SHARED_PREFERENCES_FILE_KEY = "org.thebus.boot_completed.BootCompletedPlugin"
    private const val SHARED_PREFERENCES_ITEM_KEY = "org.thebus.boot_completed.BootCompletedPlugin"

    private fun saveDartEntryPoint(entryPointHandle: Long) =
      getContext()
        .getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
              .edit()
              .putLong(SHARED_PREFERENCES_ITEM_KEY, entryPointHandle)
              .apply()

    private fun getDartEntryPoint(): Long?{

      val defaultValue: Long = -1

      var entryPoint: Long? =(
        try {
          getContext().getSharedPreferences(SHARED_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE).getLong(SHARED_PREFERENCES_ITEM_KEY, defaultValue)
        }catch(cce: ClassCastException){
          defaultValue
        }
      )

      if(entryPoint == defaultValue){
        entryPoint = null
      }

      return entryPoint
    }
  }

  override fun onMethodCall(p0: MethodCall?, p1: MethodChannel.Result?) {
    if(p0?.method == "SaveDartEntryPoint"){
      saveDartEntryPoint((p0.arguments as JSONArray).getLong(0))
      p1?.success(true)
    }
  }
}