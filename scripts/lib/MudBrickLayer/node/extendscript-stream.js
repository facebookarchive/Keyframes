/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
var net = require('net')
var PassThrough = require('readable-stream').PassThrough

var psEval = function(){
  throw Error("You need to define an eval function")
}
exports.defineEval = function(Eval){
  psEval = Eval
  return exports
}
exports.createStream = createSingleUseTCPStream

function createSingleUseTCPStream(config, callback){
  if (arguments.length === 1){
    callback = config
    config = {}
  }

  var outputPassThrough = new PassThrough()

  var server = net.createServer()
  var _host = '0.0.0.0'
  var _port = 8042
  server.listen(_port)

  server.on('listening', function(){
    if (exports.debug) console.warn('extendscript-stream listening', _host + ':' + _port)
    callback(null, _host + ':' + _port)
  })
  server.on('error', function(error){
    if (exports.debug) console.warn(error)
    if (error.code == 'EADDRINUSE') {
      server.listen(++_port, _host)
      if (exports.debug) console.warn('EADDRINUSE, trying', _port)
      return
    }
    outputPassThrough.emit('error', error)
  })
  server.on('connection', function(client){
    if (exports.debug) console.warn('connection from client', _host + ':' + _port)

    client.on('data', outputPassThrough.push.bind(outputPassThrough))
    client.on('error', outputPassThrough.emit.bind(outputPassThrough, 'error'))
    client.on('end', server.close.bind(server))
  })
  server.on('close', function(){
    if (exports.debug) console.warn('server close', _host + ':' + _port)
    outputPassThrough.push(null)
  })

  return outputPassThrough
}


exports.jsxStream = jsxStream

function jsxStream(fn, args, __jsx_prefix__){
  if (typeof fn != 'function') {
    var code = fn;
    fn = Function('STDOUT', code);
    fn.__jsx_prefix__ = code.__jsx_prefix__ || __jsx_prefix__;
  }
  fn.__jsx_prefix__ = fn.__jsx_prefix__ || __jsx_prefix__;
  var outputPassThrough = createSingleUseTCPStream(function(error, address){
    var result = psEval(applyStream_jsx, [fn, address].concat(args))
    // don't pass the return value of psEval since we're streaming the results instead
    result.on('data', function(data){ if (exports.debug) console.warn('jsxStream psEval data', String(data)) })
    result.on('end', outputPassThrough.end.bind(outputPassThrough))
    result.on('error', outputPassThrough.emit.bind(outputPassThrough, 'error'))
  })
  return outputPassThrough
}

function applyStream_jsx(fn, address){
  var args = Array.prototype.slice.call(arguments, 2)
  var socket = new Socket, result
  if (socket.open(address)){
    return fn.apply(null, [socket].concat(args))
  }
  throw Error('Cannot open socket for address "' + address + '"')
}


function outputStream_jsx(address){
  var socket = new Socket
  socket.open(address, 'UTF-8')
  return socket
}
