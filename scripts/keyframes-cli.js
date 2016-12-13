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

const evalInAE = require('./lib/MudBrickLayer/node/evalInAE');
const AECompToKeyframesAnimation = require('./lib/keyframes/AECompToKeyframesAnimation');
const fs = require('fs');

function main() {
  function get_selectedComp_jsx(send) {
    Property.prototype.ao_keyframeTweens = undefined; // disable slow tween rendering
    app.project.activeItem && send(app.project.activeItem.toJSON());
  }

  evalInAE(get_selectedComp_jsx)
    .on('data', (layer) => {
      const kfDoc = AECompToKeyframesAnimation(layer);
      fs.writeFileSync(layer.name + '.kf.json',
                       JSON.stringify(kfDoc, null, 2));
    })
    .on('error', (error) => console.error(error))
    .on('end', () => {});
}

module.exports = main;

if (module.id == '.') {
  main();
}

