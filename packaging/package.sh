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

# Print instructions for adding the icon to the package.
#echo "MarkSpace.app has now been created. You now must manually add the MarkSpace logo."
#echo "Step 1: Open the \"MarkSpace Icon.png\" file in Preview (the image viewer on MacOS)."
#echo "Step 2: Press Command-A in Preview to select the image. Then press Command-C to copy the image data. This will take 1-2 seconds because the image data is not very compressed."
#echo "Step 3: Select the \"MarkSpace.app\" file in Finder. Press Command-I to bring up the Info window for the app."
#echo "Step 4: The top-left corner of the info window should show a default icon. Click on this default icon so that it becomes selected (it should have a blue outline when selected)."
#echo "Step 5: Press Command-V to paste the image data onto the selected icon."
#echo "Step 6: Close and re-open Finder, then double-click on the app. This should cause MacOS to reload the icon and display it. If not, rebooting the computer may be necessary to clear the icon cache."
