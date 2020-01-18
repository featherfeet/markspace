#!/bin/bash

BASEDIR=$(dirname "$0")

cd "$BASEDIR"
cd ..
cd ..

./PlugIns/Java.runtime/Contents/Home/bin/java -Xdock:icon=./Contents/Resources/AppIcon.png -jar ./Java/markspace-1.0.jar
