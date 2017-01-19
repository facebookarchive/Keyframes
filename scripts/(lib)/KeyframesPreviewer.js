/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
var __dirname = File($.fileName).parent.fsName;
var adb = require('./adb.jsx');

var KeyframesPreviewer = {};

KeyframesPreviewer.apkFile = File(__dirname + '/bin-android').getFiles('*.apk')[0];

KeyframesPreviewer.isEnabled = function(){
  return adb.isEnabled();
};

KeyframesPreviewer.install = function(path) {
  if (!(KeyframesPreviewer.apkFile && KeyframesPreviewer.apkFile.exists)) {
    throw Error("Android Sample app can't be installed. File not found in " + __dirname + '/bin-android');
  }
  return adb.getDeviceSerials().map(function(info){
    var results = '';
    results += adb.call('-s', info.serial, 'uninstall', 'com.facebook.keyframes.sample.keyframes');
    results += adb.call('-s', info.serial, 'install', KeyframesPreviewer.apkFile.fsName);
    results += adb.call('-s', info.serial, 'shell', '-n', 'am start "com.facebook.keyframes.sample.keyframes/com.facebook.keyframes.sample.MainActivity"');
    return results;
  }).join('\n\n');
};

KeyframesPreviewer.previewJSONAtPath = function(path) {
  if (!File(path).exists) {
    throw Error('File not found: ' + String(path));
  }
  var serials = adb.getDeviceSerials();
  if (serials.length === 0) {
    throw Error('No Android devices found');
  }
  var remoteFile = "/sdcard/KeyframesAnimationPreview.json";
  return serials.map(function(info){
    var results = '';
    results += adb.call('-s', info.serial, 'shell', '-n', 'am start "com.facebook.keyframes.sample.keyframes/com.facebook.keyframes.sample.MainActivity"');
    results += adb.call('-s', info.serial, 'push', path, remoteFile);
    results += adb.call('-s', info.serial, 'shell', '-n', 'am broadcast -a PreviewKeyframesAnimation -e descriptorPath "' + remoteFile + '"');
    return results;
  }).join('\n\n');
};

if (typeof module == 'object') {
  module.exports = KeyframesPreviewer;
}
