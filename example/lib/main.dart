import 'package:flutter/material.dart';

import 'package:boot_completed/boot_completed.dart' as boot_completed;

import 'package:path_provider/path_provider.dart' as path_provider;
import 'dart:io';

void main() {
  runApp(MyApp());
  boot_completed.setBootCompletedFunction(incrementCounter);
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
    showCounterValue();
  }

  int _counter;

  Future<void> showCounterValue() async {
    var counterValue = await getCounter();

    setState(() {
      _counter = counterValue;
    });
  }

  void doIncrementCounter() async {
    await incrementCounter();
    await showCounterValue();
  }

  void doResetCounter() async {
    await resetCounter();
    await showCounterValue();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Counter: $_counter'),
        ),
        floatingActionButton: Column(
          children: <Widget>[
            FloatingActionButton(
              onPressed: doResetCounter,
            ),
            FloatingActionButton(
              onPressed: doIncrementCounter,
            )
          ],
          mainAxisAlignment: MainAxisAlignment.end,
        ),
      ),
    );
  }
}

final COUNTER_FILE_PATH = "myCounter";

Future<String> getPrivateDocsPath() async {
  var myDocsPath =
      (await path_provider.getApplicationDocumentsDirectory()).path;

  if (!myDocsPath.endsWith("/")) {
    myDocsPath += "/";
  }

  return myDocsPath;
}

Future<File> writeStringAsPrivateFile(String someString, String fileName,
    {bool append = false}) async {
  var writeMode = FileMode.write;

  if (append) {
    writeMode = FileMode.append;
  }

  return File((await getPrivateDocsPath()) + fileName)
      .writeAsString(someString, mode: writeMode);
}

Future<String> readPrivateFileAsString(String fileName) async {
  return await File((await getPrivateDocsPath()) + fileName).readAsString();
}

Future<int> getCounter() async {
  var currentValue;

  try {
    currentValue = int.parse(await readPrivateFileAsString(COUNTER_FILE_PATH));
  } catch (e) {
    currentValue = 0;
  }

  return currentValue;
}

Future<void> setCounter(int newCounterValue) async {
  await writeStringAsPrivateFile(newCounterValue.toString(), COUNTER_FILE_PATH);
}

Future<void> incrementCounter() async {
  var currentValue = await getCounter();

  currentValue += 1;

  await setCounter(currentValue);
}

Future<void> resetCounter() async {
  await setCounter(0);
}
