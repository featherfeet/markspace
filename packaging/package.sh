#!/bin/bash

echo "Note: This script must be run on MacOS."
echo "Note: You may need to fix the \$JAVA_HOME variable."

JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-13.0.1.jdk/Contents/Home
export JAVA_HOME

$JAVA_HOME/bin/jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.desktop,java.instrument,java.management,java.naming,java.rmi,java.scripting,java.security.jgss,java.sql,jdk.unsupported --output Home

mkdir MarkSpace
mkdir MarkSpace/Java
mkdir MarkSpace/PlugIns
mkdir MarkSpace/PlugIns/Java.runtime
mkdir MarkSpace/PlugIns/Java.runtime/Contents
mkdir MarkSpace/PlugIns/Java.runtime/Contents/Home
mkdir MarkSpace/PlugIns/Java.runtime/Contents/MacOS
mkdir MarkSpace/Resources
mkdir MarkSpace/Contents
mkdir MarkSpace/Contents/MacOS

cp ../target/markspace*.jar MarkSpace/Java/
cp -r Home/* MarkSpace/PlugIns/Java.runtime/Contents/Home/
cp $JAVA_HOME/lib/libjli.dylib MarkSpace/PlugIns/Java.runtime/Contents/MacOS/

chmod +x launcher.sh
cp launcher.sh MarkSpace/Contents/MacOS/

cp Info.plist MarkSpace/Contents/

mv MarkSpace MarkSpace.app
