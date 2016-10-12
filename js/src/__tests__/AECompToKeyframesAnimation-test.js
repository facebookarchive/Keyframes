/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 * @flow
 */
'use strict';

jest.disableAutomock(); // unmock to use the actual implementation of sum

const AECompToKeyframesAnimation = require('../AECompToKeyframesAnimation');

describe('AECompToKeyframesAnimation', () => {

  it('exists', () => {
    expect(typeof AECompToKeyframesAnimation).toEqual('function');
  });

  // TODO: Add real tests for AECompToKeyframesAnimation
});
