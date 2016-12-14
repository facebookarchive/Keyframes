/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
var lockFile = require('lockfile')
var Readable = require('readable-stream').Readable
var spawn = require('child_process').spawn
var functionToExtendScript = require('./functionToExtendScript')
function noop(){}

var lockPath = process.env.HOME + '/.evalInAfterEffects.lock'

var lockOptions = {
  wait: 5*1000,
  stale: 30*1000,
}

exports = module.exports = evalInAfterEffects

var UID = 0

evalInAfterEffects.NAME = null;

evalInAfterEffects.getName = function(){
  if (evalInAfterEffects.NAME == null) {
    try {
      ;(function(error, Applications){
        if (Applications.indexOf('Adobe After Effects CC 2019') != -1) evalInAfterEffects.NAME = "Adobe After Effects CC 2019";
        else if (Applications.indexOf('Adobe After Effects CC 2018') != -1) evalInAfterEffects.NAME = "Adobe After Effects CC 2018";
        else if (Applications.indexOf('Adobe After Effects CC 2017') != -1) evalInAfterEffects.NAME = "Adobe After Effects CC 2017";
        else if (Applications.indexOf('Adobe After Effects CC 2016') != -1) evalInAfterEffects.NAME = "Adobe After Effects CC 2016";
        else if (Applications.indexOf('Adobe After Effects CC 2015') != -1) evalInAfterEffects.NAME = "Adobe After Effects CC 2015";
        else if (Applications.indexOf('Adobe After Effects CC 2014') != -1) evalInAfterEffects.NAME = "Adobe After Effects CC 2014";
        else if (Applications.indexOf('Adobe After Effects CC') != -1) evalInAfterEffects.NAME = "Adobe After Effects CC";
        else if (Applications.indexOf('Adobe After Effects CS6') != -1) evalInAfterEffects.NAME = "Adobe After Effects CS6";
        else if (Applications.indexOf('Adobe After Effects CS5') != -1) evalInAfterEffects.NAME = "Adobe After Effects CS5";
      }(null, require('fs').readdirSync('/Applications')));
    } catch(e){}
  }
  return evalInAfterEffects.NAME;
}

function evalInAfterEffects(fn, args){
  var ID = ++UID
  var debugInfo

  if (exports.debug){
    debugInfo = {
      ID:ID,
    }
    console.warn('evalInAfterEffects', debugInfo, {
      name:evalInAfterEffects,
      fn:fn,
      args:args
    })
  }

  var readable = new Readable
  readable._read = function(size){
    readable._read = noop
    if (exports.debug) console.warn('_read', debugInfo)

    var script = functionToExtendScript(fn, args)
    var cliArgs = []

    cliArgs.push("-e", 'on run argv')
    cliArgs.push("-e", 'with timeout of 600 seconds')
    cliArgs.push("-e",   'tell application "' + evalInAfterEffects.getName() + '" to DoScript (item 1 of argv)')
    cliArgs.push("-e", 'end timeout')
    cliArgs.push("-e", 'end run')

    cliArgs.push(script)
    if (exports.debug) console.log('script `%s`', script)

    if (exports.debug) console.warn('WILL LOCK', debugInfo)

    lockFile.lock(lockPath, lockOptions, function(error, fileDescriptor){
      if (error){
        if (exports.debug) {debugInfo.error = error; console.warn('NOT LOCKED', debugInfo)}
        return readable.emit('error', error);
      }
      if (exports.debug) {console.warn('LOCKED', debugInfo)}
      if (exports.debug) {console.warn('spawn', debugInfo)}

      var child = spawn('/usr/bin/osascript', cliArgs)

      child.stdout.on('readable', function(){
        readable.push(this.read())
      })
      var _error = ''
      child.stderr.on('data', function(data){ _error += data })

      child.on('exit', function(code){
        if (exports.debug) {
          debugInfo.exitCode = code
          debugInfo.stderr = _error
          console.warn('spawn exit', debugInfo)
        }
        lockFile.unlock(lockPath, function(error){
          if (exports.debug) {
            debugInfo.exitCode = code
            debugInfo.error = error
            console.warn('UNLOCK', debugInfo)
          }
          readable.push(null)
          if (error) readable.emit('error', error);
          if (code) readable.emit('error', Error(_error));
        })
      })

    })
  }
  return readable
}
