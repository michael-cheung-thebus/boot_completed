import 'dart:async';

import 'package:path_provider/path_provider.dart' as path_provider;
import 'dart:io';
import 'dart:ui';

const _USER_BOOT_FUNCTION_HANDLE_FILENAME = "org.thebus.boot_completed.userBootFunctionHandle";

Future<int> _getHandle(Function someFunction) async => PluginUtilities.getCallbackHandle(someFunction).toRawHandle();
Future<int> _getUserBootCompletedHandle() async => int.parse(await _readPrivateFileAsString(_USER_BOOT_FUNCTION_HANDLE_FILENAME));

Future<void> setBootCompletedFunction(Function functionToExecute) async{
  await _writeStringAsPrivateFile("${await _getHandle(functionToExecute)}", _USER_BOOT_FUNCTION_HANDLE_FILENAME);
}

Future<void> _execHandle(int someHandle) async => PluginUtilities.getCallbackFromHandle(CallbackHandle.fromRawHandle(someHandle))();

//this function is called by the android side
//lint will claim that it's not used, but it totally is!
//...just in a completely terrible and non-obvious way
Future<void> _executeOnBootCompleted() async{

  //this handle should have been set by the application that's using this plugin
  //by calling setBootCompletedFunction
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

Future<File> _writeStringAsPrivateFile(String someString, String fileName) async =>
  File((await _getPrivateDocsPath()) + fileName).writeAsString(someString,mode:FileMode.write);

Future<String> _readPrivateFileAsString(String fileName) async =>
  await File((await _getPrivateDocsPath()) + fileName).readAsString();