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

    if(!deferExecution){
      handleBootCompleted(p0, p1)
    }
  }

  private fun handleBootCompleted(p0: Context?, p1: Intent?){

    if(contextRef == null){
      contextRef = SoftReference(p0!!)
    }

    val dep = dartEntryPoint

    if(dep != null) {
      doDartCallback(p0!!.applicationContext, dep)
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

      val dartEntryPointChannelName = "org.thebus.boot_completed.BootCompletedPlugin.MethodChannel"
      val dartEntryPointChannel = MethodChannel(registrar.messenger(),dartEntryPointChannelName,JSONMethodCodec.INSTANCE)

      val bcp = BootCompletedPlugin()

      dartEntryPointChannel.setMethodCallHandler(bcp)
    }

    private var contextRef: SoftReference<Context>? = null
    private fun getContext() = contextRef!!.get()!!

    private const val PREFS_FILE_NAME = "org.thebus.boot_completed.BootCompletedPlugin"
    private const val PREFS_ITEM_KEY_ENTRY_POINT = "org.thebus.boot_completed.BootCompletedPlugin.EntryPoint"
    private const val PREFS_ITEM_KEY_DEFER_EXECUTION = "org.thebus.boot_completed.BootCompletedPlugin.DeferExcecution"

    private val myPrefs
      get() = getContext().getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    private var dartEntryPoint: Long?
      get() {

        val defaultValue: Long = -1

        var entryPoint: Long? =(
          try {
            myPrefs.getLong(PREFS_ITEM_KEY_ENTRY_POINT, defaultValue)
          }catch(cce: ClassCastException){
            defaultValue
          }
        )

        if(entryPoint == defaultValue){
          entryPoint = null
        }

        return entryPoint
      }
      set(value){
        if(value != null) {
          myPrefs.edit().putLong(PREFS_ITEM_KEY_ENTRY_POINT, value).apply()
        }
      }

    private var deferExecution: Boolean
      get(){

        val defaultValue = false

        return(
          try{
            myPrefs.getBoolean(PREFS_ITEM_KEY_DEFER_EXECUTION, defaultValue)
          }catch(cce: ClassCastException){
            defaultValue
          }
        )
      }
      set(value) = myPrefs.edit().putBoolean(PREFS_ITEM_KEY_DEFER_EXECUTION, value).apply()

    /**
     * expects the same context/intent as BOOT_COMPLETED broadcast would give to onReceive
    **/
    fun handleBootCompleted(p0: Context?, p1: Intent?){
      val bcp = BootCompletedPlugin()
      bcp.handleBootCompleted(p0, p1)
    }

  }

  override fun onMethodCall(p0: MethodCall?, p1: MethodChannel.Result?) {
    if(p0?.method == "SaveDartEntryPoint"){
      dartEntryPoint = (p0.arguments as JSONArray).getLong(0)
      p1?.success(true)
    }else if (p0?.method == "DeferExcecution") {
      deferExecution = (p0.arguments as JSONArray).getBoolean(0)
      p1?.success(true)
    }
  }
}