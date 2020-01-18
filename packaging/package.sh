#!/bin/bash

# Script to generate a MacOS .app runnable application file.

# Print out some instructions for the user.
echo "Note: This script must be run on MacOS."
echo "Note: You may need to fix the \$JAVA_HOME variable in the package.sh script."

# Delete previous packaged app and related files.
./clean.sh

# Generate an empty database file with the current database schema.
rm ../src/main/resources/markspace.db
sqlite3 ../src/main/resources/markspace.db ".read ../src/main/resources/createschema.sql"

# Set the location of the Java Development Kit.
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-13.0.1.jdk/Contents/Home
export JAVA_HOME

# Generate a custom JDK containing only the dependencies needed by the program.
$JAVA_HOME/bin/jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules $($JAVA_HOME/bin/jdeps --print-module-deps --ignore-missing-deps ../target/markspace*.jar) --output Home

# Create the app file structure.
mkdir MarkSpace
mkdir MarkSpace/Java
mkdir MarkSpace/PlugIns
mkdir MarkSpace/PlugIns/Java.runtime
mkdir MarkSpace/PlugIns/Java.runtime/Contents
mkdir MarkSpace/PlugIns/Java.runtime/Contents/Home
mkdir MarkSpace/PlugIns/Java.runtime/Contents/MacOS
mkdir MarkSpace/Contents
mkdir MarkSpace/Contents/MacOS
mkdir MarkSpace/Contents/Resources

# Copy the application JAR into the app file.
cp ../target/markspace*.jar MarkSpace/Java/

# Copy the JRE and its supporting library into the app file.
cp -r Home/* MarkSpace/PlugIns/Java.runtime/Contents/Home/
cp $JAVA_HOME/lib/libjli.dylib MarkSpace/PlugIns/Java.runtime/Contents/MacOS/

# Make the launcher script for the application executable, then copy it into the app file.
chmod +x launcher.sh
cp launcher.sh MarkSpace/Contents/MacOS/

# Copy the application info file into the app file.
cp Info.plist MarkSpace/Contents/

# Copy the icon into the app file. This is used for the -Xdock option to java, but NOT used for MacOS's own icon-setting (see instructions below).
cp MarkSpace\ Icon.png MarkSpace/Contents/Resources/AppIcon.png

# Copy the splash screen into the app file. This is used for the -splash: option to java.
cp MarkSpace\ Splash\ Screen.png MarkSpace/Contents/Resources/SplashScreen.png

# Rename the app file into a .app directory.
mv MarkSpace MarkSpace.app

# Add the icon.
./generate_icns.sh
