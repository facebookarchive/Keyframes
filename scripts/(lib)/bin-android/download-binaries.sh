#!/usr/bin/env bash
cd "$(dirname "$0")"
BIN_ANDROID="$PWD"
cd "$TMPDIR"

if [[ ! -f platform-tools-latest-windows.zip ]]; then
  curl -O https://dl.google.com/android/repository/platform-tools-latest-windows.zip
fi
unzip -jn platform-tools-latest-windows.zip platform-tools/adb.exe platform-tools/AdbWinApi.dll platform-tools/AdbWinUsbApi.dll -d "$BIN_ANDROID"

if [[ ! -f platform-tools-latest-darwin.zip ]]; then
  curl -O https://dl.google.com/android/repository/platform-tools-latest-darwin.zip
fi
unzip -jn platform-tools-latest-darwin.zip platform-tools/adb -d "$BIN_ANDROID"
