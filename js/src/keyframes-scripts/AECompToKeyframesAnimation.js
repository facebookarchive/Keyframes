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

import type {KfDocument, KfAnimationGroup, KfFeature, KfValue, KfAnimatable, KfPoint, KfProperty, KfPropertyPosition, KfPropertyRotation, KfPropertyScale, KfPropertyStrokeWidth, KfTimingCurve, KfGradientStop, KfGradient, } from '../KfTypes'

import type {CompItem, AVLayer, ShapeLayer, Keyframe, PropertyWithNumberValue, PropertyWithNumberArrayValue} from '../AfterEffectsTypes';

function AECompToKeyframesAnimation(comp: CompItem): KfDocument {
  const kfDoc = {
    formatVersion: '1.0',
    name: comp.name.replace(/[^a-z]/gi, '').toUpperCase(),
    key: Number(comp.name.replace(/[^0-9]/g, '')),
    frame_rate: comp.frameRate,
    animation_frame_count: Math.round(comp.workAreaDuration * comp.frameRate),
    canvas_size: [comp.width, comp.height],
    features: [],
    animation_groups: [],
  };
  
  comp.layers
  .filter((layer) => layer.enabled)
  .forEach((layer) => {
    switch (layer.__type__) {
    case 'AVLayer':
      const group = KfAnimationGroupFromAVLayer(comp, layer, kfDoc)
      if (group) {
        kfDoc.animation_groups.push(group);
      }
      break;
    case 'ShapeLayer':
      layer
      break;
    default:
      console.warn(`Skipped unsupported layer type '${layer.__type__}'`);
    }
  });
  
  return kfDoc;
}

function KfAnimationGroupFromAVLayer(
  comp: CompItem,
  layer: AVLayer,
  kfDoc: KfDocument,
): ?KfAnimationGroup {
  /*
    A KfAnimationGroup is a Null parent of a Shape or a Null
  */
  warnIfUsingMissingFeature(layer.width !== comp.width, 'width', layer, comp);
  warnIfUsingMissingFeature(layer.height !== comp.height, 'height', layer, comp);
  const kfAnimGroup: KfAnimationGroup = {
    group_id: layer.index,
    group_name: layer.name,
    parent_group: layer.parent$index,
    animations: [],
  };

  const defaultAnchor = [layer.width / 2, layer.height / 2];
  layer.properties.forEach(layerProp => {
    switch (layerProp.matchName) {
    case 'ADBE Transform Group':
      let kfAnimGroupPropPositionAnim: KfPropertyPosition;
      let kfAnimGroupPropRotationAnim: KfPropertyRotation;
      let kfAnimGroupPropScaleAnim: KfPropertyScale;
      let kfAnimGroupPropStrokeWidthAnim: KfPropertyStrokeWidth;

      let anchor: [number, number] = defaultAnchor;
      layerProp.properties.forEach(tfProp => {
        switch (tfProp.matchName) {
        case 'ADBE Anchor Point': anchor = [tfProp.value[0], tfProp.value[1]]; break;
        }
      });

      layerProp.properties.forEach(tfProp => {
        warnIfUsingMissingFeature(!!tfProp.expression, 'expression', tfProp, layerProp, layer, comp);
        switch (tfProp.matchName) {

        case 'ADBE Anchor Point': {
          warnIfUsingMissingFeature(tfProp.keyframes && tfProp.keyframes.length > 0,
            'keyframes', tfProp, layerProp, layer, comp);
        } break;

        case 'ADBE Position': {
          const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes);
          const key_values = keyValuesFor(comp, tfProp, (value: number[]) => [value[0], value[1]]);
          if (key_values.filter(({data:[x, y]}) => x !== defaultAnchor[0] && y !== defaultAnchor[1]).length > 0) {
            kfAnimGroupPropPositionAnim = {property: 'POSITION', key_values, timing_curves, anchor};
          }
        } break;

        case 'ADBE Scale': {
          const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes);
          const key_values = keyValuesFor(comp, tfProp, (value: number[]) => [value[0], value[1]]);
          if (key_values.filter(({data:[x, y]}) => x !== 100 || y !== 100).length > 0) {
            kfAnimGroupPropScaleAnim = {property: 'SCALE', key_values, timing_curves, anchor};
          }
        } break;

        case 'ADBE Rotate Z': {
          const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes);
          const key_values = keyValuesFor(comp, tfProp, (value: number) => [value]);
          if (key_values.filter(({data:[value]}) => value !== 0).length > 0) {
            kfAnimGroupPropRotationAnim = {property: 'ROTATION', key_values, timing_curves, anchor};
          }
        } break;

        // case 'ADBE Position_0': break;
        // case 'ADBE Position_1': break;
        // case 'ADBE Position_2': break;
        // case 'ADBE Orientation': break;
        // case 'ADBE Rotate X': break;
        // case 'ADBE Rotate Y': break;
        // case 'ADBE Opacity': break;
        // case 'ADBE Envir Appear in Reflect':
        //   kfDoc.formatVersion = '1.1';
        //   break;
        default:
          warnIfUsingMissingFeature(tfProp.isModified, tfProp.__type__, tfProp, layerProp, layer, comp);
        }
      });

      kfAnimGroupPropPositionAnim && kfAnimGroup.animations.push(kfAnimGroupPropPositionAnim);
      kfAnimGroupPropRotationAnim && kfAnimGroup.animations.push(kfAnimGroupPropRotationAnim);
      kfAnimGroupPropScaleAnim && kfAnimGroup.animations.push(kfAnimGroupPropScaleAnim);
      kfAnimGroupPropStrokeWidthAnim && kfAnimGroup.animations.push(kfAnimGroupPropStrokeWidthAnim);
      break;
    default:
      if (layerProp.__type__ === 'PropertyGroup') {
        warnIfUsingMissingFeature(layerProp.properties.length > 0, layerProp.__type__, layerProp, layer, comp);
      } else {
        warnIfUsingMissingFeature(layerProp.isModified, layerProp.__type__, layerProp, layer, comp);
      }
    }
  });

  return kfAnimGroup;
}

const parseTimingFunctionsFromKeyframes = (keyframes) =>
  keyframes == null ? [] :
  keyframes.map((keyframe, index) =>
    parseTimingFunctions(keyframe, keyframes && keyframes[index + 1])
  ).filter(Boolean)
;
const parseTimingFunctions = (keyframeA, keyframeBB) =>
  keyframeA && keyframeBB &&
  [
    [0 + keyframeA.outTemporalEase[0].influence / 200, 0],
    [1 - keyframeBB.inTemporalEase[0].influence / 200, 1],
  ]
;

function keyValuesFor<T, X>(
  comp: CompItem,
  tfProp: any,
  convert: (value: T) => X,
): KfValue<X>[] {
  const keyValues = [];
  tfProp.keyframes && tfProp.keyframes.forEach(({time, value}) => {
    const data = convert(value);
    keyValues.push({start_frame: Math.round(time * comp.frameRate), data});
  });
  if (keyValues.length === 0) {
    const data = convert(tfProp.value);
    keyValues.push({start_frame: 0, data});
  }
  return keyValues;
}


function warnIfUsingMissingFeature(shouldWarn?: boolean, feature:string, ...objects: Array<Object>): void {
  if (!shouldWarn) {
    return;
  }
  const keyPath = objects.map(({name}) => name).reverse().concat(feature);
  const comp = objects[objects.length - 1];
  keyPath.unshift(comp.parentFolder$name);
  console.warn('UNSUPPORTED: %s', keyPath.join(' → '));
}

module.exports = AECompToKeyframesAnimation;
