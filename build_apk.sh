#!/usr/bin/env bash
set -e
if [ -x "./gradlew" ]; then
  ./gradlew assembleDebug
else
  echo "Gradle wrapper not found. Open the project in Android Studio or add a compatible Gradle wrapper."
  exit 2
fi
