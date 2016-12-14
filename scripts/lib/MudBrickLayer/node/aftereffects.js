/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
var aftereffects = exports
var aeEval = require('./aftereffects-eval')

var PSLIB_PATHS = [
  __dirname + '/../ExtendScript/AE/index-ae.jsxinc',
]
function pathToImport(path){ return '#include "' + path + '"' }
var PSLIB_SCRIPT = PSLIB_PATHS.map(pathToImport).join('\n')

var TMP_IMPORT_PATHS = []

////////////////////////////////////////////////////////////////////////////////

var execFile = require('child_process').execFile
var TEMPLATE = function(){
  var transaction = $TRANSACTION
  var result
  function transactionWrapper(){ result = transaction() }
  if (!(app.documents.length)) transactionWrapper()
  else app.activeDocument.suspendHistory(decodeURIComponent("$NAME"), "transactionWrapper()")
  return result
}

aftereffects.setAppName = function(appName){
  aeEval.NAME = appName
  return aftereffects
}

aftereffects.include = function(paths){
  if (paths && paths.length)
    for (var index = -1, length = paths.length; ++index < length;)
      TMP_IMPORT_PATHS.push(paths[index])
  return this
}

var aeStream = require('./aftereffects-stream').aeStream

aftereffects.createStream = function(jsx, args){
  return aeStream(jsx, args, jsx_header())
}

function jsx_header(){
  var script = PSLIB_SCRIPT + '\n' + TMP_IMPORT_PATHS.map(pathToImport).join('\n') + '\n'
  TMP_IMPORT_PATHS.length = 0
  return script
}
