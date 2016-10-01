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
const LOVE_AE = require('./love-ae.json');
const LOVE_KF = require('./love-kf.json');

describe('AECompToKeyframesAnimation', () => {
  it('exists', () => {
    expect(typeof AECompToKeyframesAnimation).toEqual('function');
  });

  let converted;
  beforeAll(() => {
    converted = AECompToKeyframesAnimation(LOVE_AE);
    // console.log(converted)
    // console.log(LOVE_KF)
    // console.log(JSON.stringify(converted.animation_groups[0].animations))
    // console.log(JSON.stringify(LOVE_KF.animation_groups[0].animations))
  });

  it('should generate all the expected animation_group keys', () => {
    Object.keys(LOVE_KF.animation_groups[0]).map(key => {
      expect(typeof converted.animation_groups[0][key])
      .toEqual(typeof LOVE_KF.animation_groups[0][key]);
    });
  });

  it('should generate all the expected keys', () => {
    Object.keys(LOVE_KF).map(key => {
      expect(typeof converted[key])
      .toEqual(typeof LOVE_KF[key]);
    });
  });

  for (let key in LOVE_KF) {
    if (typeof LOVE_KF[key] != 'object') {
      it(`converts ${key}`, () => {
        expect(converted[key]).toEqual(LOVE_KF[key]);
      });
    } else {
      it(`should converts object ${key}`, () => {
        const converted_Object = converted[key];
        const LOVE_KF_Object = LOVE_KF[key];
        expect(typeof converted_Object).toEqual(typeof LOVE_KF_Object);
        for (let key1 in LOVE_KF_Object) {
          expect(converted_Object && converted_Object[key1]).toEqual(LOVE_KF_Object[key1]);
        }
      });
    }
  }
});

