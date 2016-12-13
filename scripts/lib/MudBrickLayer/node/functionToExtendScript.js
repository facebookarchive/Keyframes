/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
module.exports = exports = function functionToExtendScript(fn, args){
  if (args == null) args = []
  else if (!Array.isArray(args)) args = [args]

  var script, _header = []
  if (fn.__jsx_prefix__) _header.push(fn.__jsx_prefix__)

  if (typeof fn == 'function'){
    script = ';(' + fn.toString() + ')'
  } else {
    script = String(fn)
  }
  if (args.length > 0 || typeof fn == 'function'){
    script += '('
    script += args.map(function(arg){
      if (typeof arg == 'function'){
        if (arg.__jsx_prefix__) _header.push(arg.__jsx_prefix__)
        return '(' + arg.toString() + ')';
      }
      else {
        return JSON.stringify(arg)
      }
    }).join(', ')
    script += ')'
  }
  var finalCode = _header.join('\n') +'\n'+ script
  if (functionToExtendScript.DEBUG) {
    console.warn('functionToExtendScript AFTER');
    console.warn(finalCode);
  }
  return finalCode;
}
