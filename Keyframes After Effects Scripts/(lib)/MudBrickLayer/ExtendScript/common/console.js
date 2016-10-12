/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */
if (typeof JSON != 'object'){
  throw new Error('JSON polyfill must be included first');
}
var console = exports;

function badConfig(){
  throw new Error('Bad config. You need to call console.configure({stdout:…, stderr:…})')
}

var stdout = {write: badConfig};
var stderr = {write: badConfig};

console.configure = function(config){
  stdout = config.stdout;
  stderr = config.stderr;
  if (!( stdout && typeof stdout.write === 'function'
      && stderr && typeof stderr.write === 'function')) {
    badConfig();
  }
  return console;
}

function argsToMessage(args) {
  var message = '';
  for (var arg, index = -1, argsLength = args.length; ++index < argsLength;) {
    arg = args[index];
    if (index > 0) {
      message += ' ';
    }
    if (typeof arg !== 'string') {
      arg = JSON.stringify(arg, null, 2);
    }
    message += '' + arg;
  }
  message && message += '\n';
  return message;
};

console.log = function(){
  stdout.write(argsToMessage(arguments));
};

console.warn = function(){
  stderr.write(argsToMessage(arguments));
};

console.error = function(error){
  if (error instanceof Error) {
    stderr.write(
      '(' + error.fileName +
      ':' + error.line +
      ':' + error.start +
      '-' + error.end +
      ') ' + error.message +
      '\n'
    );
    stderr.write(argsToMessage(Array.prototype.slice.call(arguments, 1)));
  } else {
    stderr.write(argsToMessage(arguments));
  }
  typeof $ == 'object' && stderr.write($.stack);
};

console.assert = function(True, message){
  if (!True) {
    throw new Error(message || 'Assertion Error');
  }
};

console.time = function(label) {
  console.log(label + " starting...");
  console.time['Timer ' + label] = Date.now();
};

console.timeEnd = function(label) {
  var duration = Date.now() - console.time['Timer ' + label];
  console.log(label + " Time: " + (duration / 1000));
  delete console.time['Timer ' + label];
};
