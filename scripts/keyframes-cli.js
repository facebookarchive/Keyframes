#!/usr/bin/env node

/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
'use strict';

const evalInAE = require('./(lib)/MudBrickLayer/node/evalInAE');
const AECompToKeyframesAnimation = require('./(lib)/keyframes/AECompToKeyframesAnimation');
const fs = require('fs');
const argv = require('minimist')(process.argv.slice(2));

function main() {
  function get_selectedComp_jsx(send) {
    VirtualTween.ENABLED = false; // disable slow tween rendering
    if (app.project.activeItem) {
      var items = app.project.items;
      var files = [];
      for (var i = 1; i <= app.project.items.length; ++i) {
        var item = items[i]
        if (item.file) {
          files.push(item.toJSON());
        }
      }
      var activeItem = app.project.activeItem.toJSON();
      activeItem['files'] = files;
      send(activeItem);
    }
  }

  evalInAE(get_selectedComp_jsx)
    .on('data', (layer) => {
      if (argv['raw']) {
        fs.writeFileSync(layer.name + '.raw.json', JSON.stringify(layer, null, 2));
      } else {
        const kfDoc = AECompToKeyframesAnimation(layer);
        if (layer.files.length > 0) {
          const bitmaps = {};
          for (let i = 0; i < layer.files.length; ++i) {
            const item = layer.files[i];
            if (fs.existsSync(item.file)) {
              const data = fs.readFileSync(item.file);
              bitmaps[item.name] = new Buffer(data).toString('base64');
            }
          }
          kfDoc['bitmaps'] = bitmaps;
        }
        fs.writeFileSync(layer.name + '.kf.json', JSON.stringify(kfDoc, null, 2));
      }
    })
    .on('error', (error) => console.error(error))
    .on('end', () => {})
  ;
}

module.exports = main;

if (module.id == '.') {
  main();
}
