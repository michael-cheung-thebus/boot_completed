import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'package:path_provider/path_provider.dart' as path_provider;
import 'dart:io';
import 'dart:ui';

const _USER_BOOT_FUNCTION_HANDLE_FILENAME =
    "org.thebus.boot_completed.userBootFunctionHandle";

Future<int> _getHandle(Function someFunction) async =>
    PluginUtilities.getCallbackHandle(someFunction).toRawHandle();
Future<int> _getPluginBootCompletedHandle() async =>
    await _getHandle(_executeOnBootCompleted);
Future<int> _getUserBootCompletedHandle() async => int.parse(
    await _readPrivateFileAsString(_USER_BOOT_FUNCTION_HANDLE_FILENAME));

Future<void> _execHandle(int someHandle) async =>
    PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(someHandle))();

///sets the dart function to be executed on boot completed
///
///once this is called once (i.e. by putting it in main() and launching the app once)
///the function specified will be called on boot completed.
Future<void> setBootCompletedFunction(Function functionToExecute) async {
  await _saveDartEntryPointToAndroid();
  await _writeStringAsPrivateFile("${await _getHandle(functionToExecute)}",
      _USER_BOOT_FUNCTION_HANDLE_FILENAME);
}

//this function is called by the android side
Future<void> _executeOnBootCompleted() async {
  // Ensure the application is initialized
  WidgetsFlutterBinding.ensureInitialized();
  
  //this handle should have been set by the application that's using this plugin
  //by calling setBootCompletedFunction
  final userBootCompletedHandle = await _getUserBootCompletedHandle();

  if (userBootCompletedHandle != null) {
    await _execHandle(userBootCompletedHandle);
  }
}

Future<String> _getPrivateDocsPath() async {
  var myDocsPath =
      (await path_provider.getApplicationDocumentsDirectory()).path;

  if (!myDocsPath.endsWith("/")) {
    myDocsPath += "/";
  }

  return myDocsPath;
}

Future<File> _writeStringAsPrivateFile(String someString, String fileName,
    {bool append = false}) async {
  var writeMode = FileMode.write;

  if (append) {
    writeMode = FileMode.append;
  }

  return File((await _getPrivateDocsPath()) + fileName)
      .writeAsString(someString, mode: writeMode);
}

Future<String> _readPrivateFileAsString(String fileName) async {
  return await File((await _getPrivateDocsPath()) + fileName).readAsString();
}

MethodChannel _pluginMethodChannel() => MethodChannel(
    'org.thebus.boot_completed.BootCompletedPlugin.MethodChannel',
    JSONMethodCodec());

//invoke android method to save entry point handle
Future<bool> _saveDartEntryPointToAndroid() async =>
    await _pluginMethodChannel().invokeMethod<bool>(
        'SaveDartEntryPoint', <dynamic>[await _getPluginBootCompletedHandle()]);

///If true, will disable automatic execution on boot of the function
///set by setBootCompletedFunction
///
///you can then call (kotlin) BootCompletePlugin.handleBootCompleted elsewhere
///
///mainly useful if you have other boot receivers
///and want to execute them in a particular order
Future<bool> setDeferExecution(bool deferExecution) async =>
    await _pluginMethodChannel()
        .invokeMethod<bool>('DeferExcecution', <dynamic>[deferExecution]);
