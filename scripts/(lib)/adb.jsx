/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
var __dirname = File($.fileName).parent.fsName;
var isMac = $.os.indexOf('Macintosh OS') === 0;

var adb = {
  executable: File(__dirname + '/bin-android/adb' + (isMac ? '' : '.exe')),

  isEnabled: function(){
    return adb.executable.exists;
  },
  
  call: function(){
    if (!adb.isEnabled()) {
      throw Error('Android Debug Bridge executable is not installed. Expected to find it at '
        + JSON.stringify(adb.executable));
    }
    var args = Array.prototype.slice.call(arguments, 0);
    var command = '"' + adb.executable.fsName + '" ' + args.map(function(arg){return JSON.stringify(arg);}).join(' ');
    console.log(command);
    var results = system.callSystem(command);
    console.log(results);
    return results;
  },

  getDeviceSerials: function(){
    var devicesRaw = adb.call('devices');
    return devicesRaw.trim().split('\n')
        .filter(function(line){return line.indexOf('*') === -1;})
        .slice(1).map(function(line){
      var parts = line.split(/\s+/);
      return {
        serial: parts[0],
        status: parts[1],
      };
    });
  },
};

if (typeof module == 'object') {
  module.exports = adb;
}
