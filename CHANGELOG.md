## 0.2.0

allow execution of the boot function to be put off
by calling setDeferExecution(true)

this may or may not be useful
if for instance you have other boot receivers
and you want to execute them in a certain order

you will then have to call BootCompletedPlugin.handleBootCompleted yourself
probably from within one of the other receivers' onReceive

## 0.1.1+1

ran flutter format on lib\boot_completed.dart

## 0.1.1

changed implementation of boot completed receiver
back to the way that uses a method channel
since it seems like hardcoding the dart function name
only works in debug builds - with release builds, it's no longer functional

## 0.1.0+3

revised readmes a bit more, added link to github repo in pubspec
added dartdoc comment
confirmed compatibility with path_provider 1.0.0, changed dependency to ">=0.5.0+1 <2.0.0"

## 0.1.0+2

change http url to https

## 0.1.0+1

pub.dev analysis now finds the description is too long - shortened it

## 0.1.0

Update version number and a few non-functional things suggested by pub.dev analysis.

## 0.0.1

Initial version with working example.