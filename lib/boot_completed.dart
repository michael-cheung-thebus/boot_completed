import 'dart:async';

import 'package:flutter/services.dart';

import 'package:path_provider/path_provider.dart' as path_provider;
import 'dart:io';
import 'dart:ui';

final _USER_BOOT_FUNCTION_HANDLE_FILENAME = "org.thebus.boot_completed.userBootFunctionHandle";

Future<int> _getHandle(Function someFunction) async => PluginUtilities.getCallbackHandle(someFunction).toRawHandle();
Future<int> _getPluginBootCompletedHandle() async => await _getHandle(_executeOnBootCompleted);
Future<int> _getUserBootCompletedHandle() async => int.parse(await _readPrivateFileAsString(_USER_BOOT_FUNCTION_HANDLE_FILENAME));

Future<void> _execHandle(int someHandle) async => PluginUtilities.getCallbackFromHandle(CallbackHandle.fromRawHandle(someHandle))();

Future<void> setBootCompletedFunction(Function functionToExecute) async{
  await _saveDartEntryPointToAndroid();
  await _writeStringAsPrivateFile("${await _getHandle(functionToExecute)}", _USER_BOOT_FUNCTION_HANDLE_FILENAME);
}

//this function is called by the android side
Future<void> _executeOnBootCompleted() async{

  //this handle should have been set by the application
  //calling setBootCompletedFunction
  final userBootCompletedHandle = await _getUserBootCompletedHandle();

  if(userBootCompletedHandle != null){
    await _execHandle(userBootCompletedHandle);
  }
}

Future<String> _getPrivateDocsPath() async{

  var myDocsPath = (await path_provider.getApplicationDocumentsDirectory()).path;

  if(!myDocsPath.endsWith("/")){
    myDocsPath += "/";
  }

  return myDocsPath;
}

Future<File> _writeStringAsPrivateFile(String someString, String fileName, {bool append=false}) async{

  var writeMode = FileMode.write;

  if(append){
    writeMode = FileMode.append;
  }

  return File((await _getPrivateDocsPath()) + fileName).writeAsString(someString,mode:writeMode);
}

Future<String> _readPrivateFileAsString(String fileName) async{
  return await File((await _getPrivateDocsPath()) + fileName).readAsString();
}

//invoke android method to save entry point handle
Future<bool> _saveDartEntryPointToAndroid() async{

  const String _channelName = 'org.thebus.boot_completed.SaveDartEntryPoint';
  const MethodChannel _channel = MethodChannel(_channelName, JSONMethodCodec());

  return await _channel.invokeMethod<bool>(
      'SaveDartEntryPoint',
      <dynamic>[await _getPluginBootCompletedHandle()]
  );
}