#!/bin/bash

BASEDIR=$(dirname "$0")

cd "$BASEDIR"
cd ..
cd ..

./PlugIns/Java.runtime/Contents/Home/bin/java -Xdock:icon=./Contents/Resources/AppIcon.png -splash:./Contents/Resources/SplashScreen.png -jar ./Java/markspace-0.0.2.jar
