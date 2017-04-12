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

var macOS = {};
macOS.isTheCurrentOS = $.os.indexOf('Macintosh OS') === 0;
macOS.doAppleScript = function(script){
  return system.callSystem('/usr/bin/osascript -e ' + JSON.stringify(script));
};
macOS.openFileWithApp = function(file, app){
  if (!File(app).exists) throw Error('App not found "' + app + '"');
  if (!File(file).exists) throw Error('File not found "' + file + '"');
  return system.callSystem('open -a ' + JSON.stringify(File(app).fsName) + ' ' + JSON.stringify(File(file).fsName));
};

var KeyframesPreviewer = {};

KeyframesPreviewer.apkFile = File(__dirname + '/bin-android').getFiles('*.apk')[0];

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

  if (macOS.isTheCurrentOS) {
    var macOSPlayerApp = File(__dirname + '/bin-android/Keyframes Player.app');
    if (!macOSPlayerApp.exists) macOSPlayerApp = File('/Applications/Keyframes Player.app');
    if (!macOSPlayerApp.exists) macOSPlayerApp = File('~/Downloads/Keyframes Player.app');
    if (!macOSPlayerApp.exists) {
      console.warn('"Keyframes Player.app" not found. Install it in /Applications');
    } else {
      // Remove this once Keyframes Player reloads modified animations properly
      macOS.doAppleScript('tell the application "Keyframes Player" to quit');
      macOS.openFileWithApp(path, macOSPlayerApp);
    }
  }

  if (adb.isEnabled()) {
    var serials = adb.getDeviceSerials();
    if (serials.length === 0) {
      console.warn('No Android devices found');
      return;
    }
    var remoteFile = "/sdcard/KeyframesAnimationPreview.json";
    return serials.map(function(info){
      var results = '';
      results += adb.call('-s', info.serial, 'shell', '-n', 'am start "com.facebook.keyframes.sample.keyframes/com.facebook.keyframes.sample.MainActivity"');
      results += adb.call('-s', info.serial, 'push', path, remoteFile);
      results += adb.call('-s', info.serial, 'shell', '-n', 'am broadcast -a PreviewKeyframesAnimation -e descriptorPath "' + remoteFile + '"');
      return results;
    }).join('\n\n');
  }
};

if (typeof module == 'object') {
  module.exports = KeyframesPreviewer;
}
