#!/usr/bin/env bash -l
set -x

LocalFile="$1"
RemoteFile="/sdcard/PreviewKeyframesAnimation-$(/sbin/md5 -q "$LocalFile").json"

function push-the-animation {
  local serial="$1"
  "$ANDROID_HOME/platform-tools/adb" -s $serial push "$LocalFile" "$RemoteFile" && \
  "$ANDROID_HOME/platform-tools/adb" -s $serial shell am start "com.facebook.keyframes.sample.keyframes/com.facebook.keyframes.sample.MainActivity" && \
  "$ANDROID_HOME/platform-tools/adb" -s $serial shell am broadcast \
    -a PreviewKeyframesAnimation \
    -e descriptorPath "$RemoteFile"
}

function main {
  while read serial
  do
    [[ $serial == '' ]] && break
    echo "serial='$serial'"
    push-the-animation $serial &

  done < <(adb devices | awk 'NR>1 {print $1}')
}

main
