/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-sample file in the root directory of this source tree.
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
