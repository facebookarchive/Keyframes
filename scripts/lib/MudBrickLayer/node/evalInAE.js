/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
'use strict';

const createJSONParseThroughStream = require('JSONStream').parse;
const {createStream} = require('./aftereffects');

function evalInAE_onEach_jsx(stream, value) {
  stream.writeln(JSON.stringify(value));
}

function evalInAE_jsx(
  stream,
  jsx,
  evalInAE_onEach_jsx
) {
  jsx(evalInAE_onEach_jsx.bind(this, stream));
  stream.close();
}

function evalInAE(jsx) {
  return createStream(evalInAE_jsx, [jsx, evalInAE_onEach_jsx]).pipe(createJSONParseThroughStream());
}

module.exports = evalInAE;
