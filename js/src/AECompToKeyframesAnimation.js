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

import type {
  KfDocument,
  KfAnimationGroup,
  KfFeature,
  KfValue,
  KfAnimatable,
  KfPoint,
  KfProperty,
  KfPropertyAnchorPoint,
  KfPropertyXPosition,
  KfPropertyYPosition,
  KfPropertyRotation,
  KfPropertyScale,
  KfPropertyOpacity,
  KfPropertyStrokeWidth,
  KfTimingCurve,
  KfGradientColorStop,
  KfGradientRampStop,
  KfGradient,
  KfPathTrim,
  KfPathTrimStop,
} from '../KfTypes'

import type {
  CompItem,
  AVLayer,
  ShapeLayer,
  Shape,
  Keyframe,
  PropertyWithNumberValue,
  PropertyWithNumberArrayValue,
  PropertyGroupRootVectorsGroup,
  PropertyGroupVectorGroup,
  PropertyGroupVectorsGroup,
  PropertyGroupVectorShapeGroup,
  PropertyVectorShape,
  PropertyGroupVectorGraphicFill,
  PropertyVectorFillColor,
  PropertyGroupTransformGroup,
  PropertyGroupVectorGraphicStroke,
  PropertyVectorStrokeColor,
  PropertyVectorStrokeWidth,
  PropertyVectorStrokeLineCap,
  PropertyGroupVectorTransformGroup,
  PropertyGroupVectorFilterTrim,
  PropertyVectorAnchor,
  PropertyVectorPosition,
  PropertyVectorScale,
  PropertyVectorRotation,
  PropertyVectorGroupOpacity,
  PropertyVectorTrimStart,
  PropertyVectorTrimEnd,
  PropertyVectorTrimOffset,
  PropertyGroupEffectParade,
  PropertyGroupRamp,
  PropertyRamp0001,
  PropertyRamp0002,
  PropertyRamp0003,
  PropertyRamp0004,
  PropertyRamp0005,
} from '../AfterEffectsTypes';

function AECompToKeyframesAnimation(comp: CompItem): KfDocument {
  const kfDoc = {
    formatVersion: '1.1',
    name: comp.name.replace(/[^a-z]/gi, '').toUpperCase(),
    key: Number(comp.name.replace(/[^0-9]/g, '')),
    frame_rate: comp.frameRate,
    animation_frame_count: Math.round(comp.workAreaDuration * comp.frameRate),
    canvas_size: [comp.width, comp.height],
    features: [],
    animation_groups: [],
  };

  let lastMasking: ?KfFeature = null;

  comp.layers
  .filter((layer) => layer.enabled || layer.isTrackMatte)
  .forEach((layer) => {
    switch (layer.__type__) {
    case 'AVLayer':
      if (isBitmapLayer(layer)) {
        // a image backed layer
        const image = KFBitmapFeatureFromAVLayer(comp, layer, kfDoc);
        if (image) {
          kfDoc.features.unshift(image);
        }
      } else {
        const group = KfAnimationGroupFromAVLayer(comp, layer, kfDoc);
        if (group) {
          kfDoc.animation_groups.unshift(group);
        }
      }
      break;
    case 'ShapeLayer':
      const shape = KfFeatureFromShapeLayer(comp, layer, kfDoc);
      if (shape) {
        if (layer.isTrackMatte) {
          // is a masking layer
          lastMasking = shape;
        } else {
          if (layer.hasTrackMatte && lastMasking) {
            shape.masking = lastMasking;
          }
          kfDoc.features.unshift(shape);
        }
      }
      break;
    default:
      console.warn(`Skipped unsupported layer type '${layer.__type__}'`);
    }
  });

  return kfDoc;
}

function isBitmapLayer(layer: AVLayer | ShapeLayer): bool {
  return layer['source$__type__'] === 'FootageItem' && layer['source$name'].endsWith('.png');
}

function commonFeatureFromLayer(
  comp: CompItem,
  layer: AVLayer | ShapeLayer,
  kfDoc: KfDocument,
): KfFeature {
  const kfFeature: KfFeature = {
    name: layer.name,
    feature_id: layer.index,
  };
  if (layer.height !== comp.height || layer.width !== comp.width) {
    kfFeature.size = [layer.width, layer.height];
  }
  if (layer.parent$index) {
    kfFeature.animation_group = layer.parent$index;
  }
  if (layer.inPoint > 0) {
    kfFeature.from_frame = Math.round(layer.inPoint * kfDoc.frame_rate);
  }
  if (Math.round(layer.outPoint * kfDoc.frame_rate) < kfDoc.animation_frame_count) {
    kfFeature.to_frame = Math.round(layer.outPoint * kfDoc.frame_rate);
  }

  // layer transforms
  const transformGroup: ?PropertyGroupTransformGroup = getPropertyChild(layer, 'ADBE Transform Group');
  if (transformGroup) {
    kfFeature.feature_animations = parseTransformGroup(transformGroup, comp, layer, kfDoc);
  }
  return kfFeature;
}

function KFBitmapFeatureFromAVLayer(
  comp: CompItem,
  layer: AVLayer,
  kfDoc: KfDocument,
): ?KfFeature {
  const kfFeature = commonFeatureFromLayer(comp, layer, kfDoc);
  kfFeature.backed_image = layer['source$name'];
  return kfFeature;
}

function KfFeatureFromShapeLayer(
  comp: CompItem,
  layer: ShapeLayer,
  kfDoc: KfDocument,
): ?KfFeature {
  const kfFeature = commonFeatureFromLayer(comp, layer, kfDoc);

  // layer shape
  const rootVectorsGroup: ?PropertyGroupRootVectorsGroup = getPropertyChild(layer, 'ADBE Root Vectors Group');
  if (!rootVectorsGroup) {
    throw 'Root Vectors Group missing, corrupted input JSON';
  }
  const {
    vectorShape,
    vectorShapeEllipse,
    vectorShapeRectangle,
    vectorFillColor,
    vectorStrokeColor,
    vectorStrokeWidth,
    vectorStrokeLineCap,
    vectorPosition,
    vectorScale,
    vectorRotation,
    vectorOpacity,
    vectorTrimStart,
    vectorTrimEnd,
    vectorTrimOffset,
  } = parseRootVectorsGroup(rootVectorsGroup);

  let shapeOffset = [0, 0];

  if (vectorPosition) {
    if (vectorPosition.keyframes) {
      warnIfUsingMissingFeature(true, 'animation on vector position', vectorPosition, rootVectorsGroup, layer, comp);
    }
    shapeOffset = [vectorPosition.value[0], vectorPosition.value[1]];
  }
  if (vectorScale) {
    const key_values = keyValuesFor(comp, vectorScale, (value: number[]) => [value[0], value[1]]);
    warnIfUsingMissingFeature(key_values.filter(({data:[x, y]}) => x !== 100 || y !== 100).length > 0, 'Scale on vector', vectorScale, rootVectorsGroup, layer, comp);
  }
  if (vectorRotation) {
    const key_values = keyValuesFor(comp, vectorRotation, (value: number) => [value % 360]);
    warnIfUsingMissingFeature(key_values.filter(({data:[value]}) => value !== 0).length > 0, 'Rotation on vector', vectorRotation, rootVectorsGroup, layer, comp);
  }

  if (vectorStrokeLineCap) {
    switch (vectorStrokeLineCap.value) {
    case 1:
      kfFeature.stroke_line_cap = 'butt';
      break;
    case 2:
      kfFeature.stroke_line_cap = 'round';
      break;
    case 3:
      kfFeature.stroke_line_cap = 'square';
      break;
    }
  }

  if (vectorShape) {
    // set vector shapes
    if (vectorShape.keyframes) {
      kfFeature.key_frames = vectorShape.keyframes.map(keyframe => {
        return {
          start_frame: Math.round(keyframe.time * comp.frameRate),
          data: parseShape(keyframe.value, shapeOffset),
        };
      });
      kfFeature.timing_curves = parseTimingFunctionsFromKeyframes(vectorShape.keyframes, parseShapeMorphTimingFunctions);
    } else {
      kfFeature.key_frames = [{
        start_frame: 0,
        data: parseShape(vectorShape.value, shapeOffset),
      }];
    }
  } else if (vectorShapeEllipse) {
    const ellipseSize: ?Object =
      getPropertyChild(vectorShapeEllipse, 'ADBE Vector Ellipse Size');
    const ellipsePosition: ?Object =
      getPropertyChild(vectorShapeEllipse, 'ADBE Vector Ellipse Position');

    if (ellipseSize && ellipsePosition) {
      const keyframes = parseEllipseKeyframes(ellipseSize, ellipsePosition);

      if (keyframes.length > 0) {
        kfFeature.key_frames = keyframes.map(keyframe => {
          return {
            start_frame: Math.round(keyframe.time * comp.frameRate),
            data: makeEllipse(
              keyframe.size,
              [
                shapeOffset[0] + keyframe.position[0],
                shapeOffset[1] + keyframe.position[1]
              ],
            ),
          };
        });
        // Attempt to export the timing functions from the keyframes.
        // Only a single set of timing curves is supported for shape animation.
        // Check the ellipse size property first since it is more likely to be
        // used and the position can also be animated at the shape layer level.
        if (ellipseSize['numKeys'] === keyframes.length) {
          kfFeature.timing_curves = parseTimingFunctionsFromKeyframes(
            ellipseSize['keyframes'],
            parseTimingFunctions
          );
        } else if (ellipsePosition['numKeys'] === keyframes.length) {
          kfFeature.timing_curves = parseTimingFunctionsFromKeyframes(
            ellipsePosition['keyframes'],
            parseTimingFunctions
          );
        } else {
          console.warn('Timing functions were not exported for ellipse shape, defaulting to linear timing.\n',
                       'To use custom timing functions, ensure keyframes match for all ellipse properties.');
        }
      } else {
        const dimensions: number[] = ellipseSize['value'];
        const localOffset: number[] = ellipsePosition['value'];

        kfFeature.key_frames = [{
          start_frame: 0,
          data: makeEllipse(
            [dimensions[0], dimensions[1]],
            [shapeOffset[0] + localOffset[0], shapeOffset[1] + localOffset[1]],
          ),
        }];
      }
    }
  } else if (vectorShapeRectangle) {
    const rectSize: ?Object =
      getPropertyChild(vectorShapeRectangle, 'ADBE Vector Rect Size');
    const rectPosition: ?Object =
      getPropertyChild(vectorShapeRectangle, 'ADBE Vector Rect Position');
    const rectRoundness: ?Object =
      getPropertyChild(vectorShapeRectangle, 'ADBE Vector Rect Roundness');
    if (rectSize && rectPosition && rectRoundness) {
      const keyframes = parseRectangleKeyframes(
        rectSize,
        rectPosition,
        rectRoundness
      );

      if (keyframes.length > 0) {
        kfFeature.key_frames = keyframes.map(keyframe => {
          return {
            start_frame: Math.round(keyframe.time * comp.frameRate),
            data: makeRectangle(
              keyframe.size,
              [
                shapeOffset[0] + keyframe.position[0],
                shapeOffset[1] + keyframe.position[1]
              ],
              keyframe.roundness
            ),
          };
        });
        // Attempt to export the timing functions from the keyframes.
        // Only a single set of timing curves is supported for shape animation.
        // Check the rect size property first since it's probably the most
        // likely to be animated. Check position last since this can also be
        // animated at the shape layer level.
        if (rectSize['numKeys'] === keyframes.length) {
          kfFeature.timing_curves = parseTimingFunctionsFromKeyframes(
            rectSize['keyframes'],
            parseTimingFunctions
          );
        } else if (rectRoundness['numKeys'] === keyframes.length) {
          kfFeature.timing_curves = parseTimingFunctionsFromKeyframes(
            rectRoundness['keyframes'],
            parseTimingFunctions
          );
        } else if (rectPosition['numKeys'] === keyframes.length) {
          kfFeature.timing_curves = parseTimingFunctionsFromKeyframes(
            rectPosition['keyframes'],
            parseTimingFunctions
          );
        } else {
          console.warn('Timing functions were not exported for rect shape, defaulting to linear timing.\n',
                       'To use custom timing functions, ensure keyframes match for all rect properties.');
        }

      } else {
        const dimensions: number[] = rectSize['value'];
        const localOffset: number[] = rectPosition['value'];
        const roundness: ?number = rectRoundness['value'] > 0
          ? rectRoundness['value']
          : null;

        kfFeature.key_frames = [{
          start_frame: 0,
          data: makeRectangle(
            [dimensions[0], dimensions[1]],
            [shapeOffset[0] + localOffset[0], shapeOffset[1] + localOffset[1]],
            roundness,
          ),
        }];
      }
    }
  }

  if (vectorFillColor) {
    // set fill color
    kfFeature.fill_color = getHexColorStringFromRGB(vectorFillColor.value);
    if (vectorFillColor.keyframes && vectorFillColor.keyframes.length > 1) {
      if (!kfFeature.feature_animations) {
        kfFeature.feature_animations = [];
      }
      kfFeature.feature_animations.push({
        property: 'FILL_COLOR',
        key_values: keyValuesFor(comp, vectorFillColor, (value: number[]) => getHexColorStringFromRGB(value)),
        timing_curves: parseTimingFunctionsFromKeyframes(vectorFillColor.keyframes, parseTimingFunctions),
      });
    }
  }
  if (vectorStrokeColor) {
    // set stroke color
    kfFeature.stroke_color = getHexColorStringFromRGB(vectorStrokeColor.value);
    if (vectorStrokeColor.keyframes && vectorStrokeColor.keyframes.length > 1) {
      if (!kfFeature.feature_animations) {
        kfFeature.feature_animations = [];
      }
      kfFeature.feature_animations.push({
        property: 'STROKE_COLOR',
        key_values: keyValuesFor(comp, vectorStrokeColor, (value: number[]) => getHexColorStringFromRGB(value)),
        timing_curves: parseTimingFunctionsFromKeyframes(vectorStrokeColor.keyframes, parseTimingFunctions),
      });
    }
  }
  if (vectorStrokeWidth) {
    // set stroke width
    kfFeature.stroke_width = vectorStrokeWidth.value;
  }

  // handle gradient ramp effects
  const effects: ?PropertyGroupEffectParade = getPropertyChild(layer, 'ADBE Effect Parade');
  if (effects) {
    const rampEffect: ?PropertyGroupRamp = getPropertyChild(effects, 'ADBE Ramp');
    if (rampEffect) {
      const startPoint: ?PropertyRamp0001 = getPropertyChild(rampEffect, 'ADBE Ramp-0001');
      const startColor: ?PropertyRamp0002 = getPropertyChild(rampEffect, 'ADBE Ramp-0002');
      const endPoint: ?PropertyRamp0003 = getPropertyChild(rampEffect, 'ADBE Ramp-0003');
      const endColor: ?PropertyRamp0004 = getPropertyChild(rampEffect, 'ADBE Ramp-0004');
      const gradientType: ?PropertyRamp0005 = getPropertyChild(rampEffect, 'ADBE Ramp-0005');

      if (startPoint && startColor && endPoint && endColor && gradientType) {

        warnIfUsingMissingFeature(gradientType.value === 2, 'radial type gradient', rampEffect, effects, layer, comp);

        const kfGradient: KfGradient = {
          gradient_type: gradientType.value === 1 ? 'linear' : 'radial',
          color_start: {
            key_values: keyValuesFor(comp, startColor, (value: number[]) => getHexColorStringFromRGB(value)),
            timing_curves: parseTimingFunctionsFromKeyframes(startColor.keyframes, parseTimingFunctions),
          },
          color_end: {
            key_values: keyValuesFor(comp, endColor, (value: number[]) => getHexColorStringFromRGB(value)),
            timing_curves: parseTimingFunctionsFromKeyframes(endColor.keyframes, parseTimingFunctions),
          },
          ramp_start: {
            key_values: keyValuesFor(comp, startPoint, (value: number[]) => [value[0], value[1]]),
            timing_curves: parseTimingFunctionsFromKeyframes(startPoint.keyframes, parseTimingFunctions),
          },
          ramp_end: {
            key_values: keyValuesFor(comp, endPoint, (value: number[]) => [value[0], value[1]]),
            timing_curves: parseTimingFunctionsFromKeyframes(endPoint.keyframes, parseTimingFunctions),
          },
        }
        kfFeature.effects = {
          gradient: kfGradient,
        };
      } else {
        throw 'Gradient property missing, corrupted JSON.';
      }

    }

    if (vectorTrimStart && vectorTrimEnd && vectorTrimOffset) {
      kfFeature.path_trim = {
        path_trim_start: {
          key_values: keyValuesFor(comp, vectorTrimStart, (value: number) => [value]),
          timing_curves: parseTimingFunctionsFromKeyframes(vectorTrimStart.keyframes, parseTimingFunctions),
        },
        path_trim_end: {
          key_values: keyValuesFor(comp, vectorTrimEnd, (value: number) => [value]),
          timing_curves: parseTimingFunctionsFromKeyframes(vectorTrimEnd.keyframes, parseTimingFunctions),
        },
        path_trim_offset: {
          key_values: keyValuesFor(comp, vectorTrimOffset, (value: number) => [value]),
          timing_curves: parseTimingFunctionsFromKeyframes(vectorTrimOffset.keyframes, parseTimingFunctions),
        },
      };
    }
  }


  return kfFeature;
}

function componentToHex(
  c: number
): string {
  const hex = Math.round(c * 255).toString(16);
  return hex.length === 1 ? '0' + hex : hex;
}

function getHexColorStringFromRGB(
  [
    r: number,
    g: number,
    b: number,
    a: number,
  ],
): string {
  return "#" + componentToHex(a) + componentToHex(r) + componentToHex(g) + componentToHex(b);
}

function getPropertyChild(
  layer: ?Object,
  childName: string,
): ?Object {
  let result: ?Object = null;
  if (layer) {
    layer.properties.forEach(property => {
      if (property.matchName === childName) {
        result = property;
      }
    });
  }
  return result;
}

function parseRootVectorsGroup(
  rootGroup: ?PropertyGroupRootVectorsGroup,
): {
  vectorShape: ?PropertyVectorShape,
  vectorShapeEllipse: ?Object,
  vectorShapeRectangle: ?Object,
  vectorFillColor: ?PropertyVectorFillColor,
  vectorStrokeColor: ?PropertyVectorStrokeColor,
  vectorStrokeWidth: ?PropertyVectorStrokeWidth,
  vectorStrokeLineCap: ?PropertyVectorStrokeLineCap,
  vectorAnchor: ?PropertyVectorAnchor,
  vectorPosition: ?PropertyVectorPosition,
  vectorScale: ?PropertyVectorScale,
  vectorRotation: ?PropertyVectorRotation,
  vectorOpacity: ?PropertyVectorGroupOpacity,
  vectorTrimStart: ?PropertyVectorTrimStart,
  vectorTrimEnd: ?PropertyVectorTrimEnd,
  vectorTrimOffset: ?PropertyVectorTrimOffset,
} {
  const vectorGroup: ?PropertyGroupVectorGroup =
    getPropertyChild(rootGroup, 'ADBE Vector Group');
  const groupVectorsGroup: ?PropertyGroupVectorsGroup =
    getPropertyChild(vectorGroup, 'ADBE Vectors Group');
  const transformGroup: ?PropertyGroupVectorTransformGroup =
    getPropertyChild(vectorGroup, 'ADBE Vector Transform Group');

  const groupVectorShapeGroup: ?PropertyGroupVectorShapeGroup =
    getPropertyChild(groupVectorsGroup, 'ADBE Vector Shape - Group');
  const vectorShape: ?PropertyVectorShape =
    getPropertyChild(groupVectorShapeGroup, 'ADBE Vector Shape');
  const vectorShapeEllipse: ?Object =
    getPropertyChild(groupVectorsGroup, 'ADBE Vector Shape - Ellipse');
  const vectorShapeRectangle: ?Object =
    getPropertyChild(groupVectorsGroup, 'ADBE Vector Shape - Rect');

  const vectorGraphicFill: ?PropertyGroupVectorGraphicFill =
    getPropertyChild(groupVectorsGroup, 'ADBE Vector Graphic - Fill');
  const vectorFillColor: ?PropertyVectorFillColor =
    getPropertyChild(vectorGraphicFill, 'ADBE Vector Fill Color');

  const vectorGraphicStroke: ?PropertyGroupVectorGraphicStroke =
    getPropertyChild(groupVectorsGroup, 'ADBE Vector Graphic - Stroke');
  const vectorStrokeColor: ?PropertyVectorStrokeColor =
    getPropertyChild(vectorGraphicStroke, 'ADBE Vector Stroke Color');
  const vectorStrokeWidth: ?PropertyVectorStrokeWidth =
    getPropertyChild(vectorGraphicStroke, 'ADBE Vector Stroke Width');
  const vectorStrokeLineCap: ?PropertyVectorStrokeLineCap =
    getPropertyChild(vectorGraphicStroke, 'ADBE Vector Stroke Line Cap');

  const vectorAnchor: ?PropertyVectorAnchor =
    getPropertyChild(transformGroup, 'ADBE Vector Anchor');
  const vectorPosition: ?PropertyVectorPosition =
    getPropertyChild(transformGroup, 'ADBE Vector Position');
  const vectorScale: ?PropertyVectorScale =
    getPropertyChild(transformGroup, 'ADBE Vector Scale');
  const vectorRotation: ?PropertyVectorRotation =
    getPropertyChild(transformGroup, 'ADBE Vector Rotation');
  const vectorOpacity: ?PropertyVectorGroupOpacity =
    getPropertyChild(transformGroup, 'ADBE Vector Group Opacity');

  let groupVectorFilterTrim: ?PropertyGroupVectorFilterTrim =
    getPropertyChild(rootGroup, 'ADBE Vector Filter - Trim');
  if (!groupVectorFilterTrim) {
    groupVectorFilterTrim = getPropertyChild(groupVectorsGroup, 'ADBE Vector Filter - Trim');
  }
  const vectorTrimStart: ?PropertyVectorTrimStart =
    getPropertyChild(groupVectorFilterTrim, 'ADBE Vector Trim Start');
  const vectorTrimEnd: ?PropertyVectorTrimEnd =
    getPropertyChild(groupVectorFilterTrim, 'ADBE Vector Trim End');
  const vectorTrimOffset: ?PropertyVectorTrimEnd =
    getPropertyChild(groupVectorFilterTrim, 'ADBE Vector Trim Offset');

  return {
    vectorShape,
    vectorShapeEllipse,
    vectorShapeRectangle,
    vectorFillColor,
    vectorStrokeColor,
    vectorStrokeWidth,
    vectorStrokeLineCap,
    vectorAnchor,
    vectorPosition,
    vectorScale,
    vectorRotation,
    vectorOpacity,
    vectorTrimStart,
    vectorTrimEnd,
    vectorTrimOffset,
  };
}

type SVGCommand = {
  command: 'M' | 'C' | 'Q' | 'L',
  vertices: Array<[number, number]>,
};

function hasTangent(
  [
    x: number,
    y: number,
  ],
): bool {
  return !!x || !!y;
}

function getTangentCount(
  pair1: [number, number],
  pair2: [number, number],
): number {
  let result = 0;
  if (hasTangent(pair1)) {
    ++result;
  }
  if (hasTangent(pair2)) {
    ++result;
  }
  return result;
}

/** parses a Shape to a SVG fashion string format */
function parseShape(
  shape: Shape,
  shapeOffset: number[],
): string[] {
  const {
    inTangents,
    outTangents,
    closed,
  } = shape;

  const vertices =
    shape.vertices.map(vertex => [vertex[0] + shapeOffset[0], vertex[1] + shapeOffset[1]]);
  if (vertices.length === 0) {
    return [];
  }

  const commands: SVGCommand[] = [];
  commands.push({
    command: 'M',
    vertices: [vertices[0]],
  });
  let currIndex: number;
  let prevIndex: number;

  for (let i = 1; i <= vertices.length; ++i) {
    if (i === vertices.length) {
      if (!closed) {
        continue;
      }
      prevIndex = vertices.length - 1;
      currIndex = 0;
    } else {
      prevIndex = i - 1;
      currIndex = i;
    }
    const prevVertex = vertices[prevIndex];
    const currVertex = vertices[currIndex];
    const tangentCount = getTangentCount(outTangents[prevIndex], inTangents[currIndex]);

    if (tangentCount === 2) {
      commands.push({
        command: 'C',
        vertices: [
          [
            prevVertex[0] + outTangents[prevIndex][0],
            prevVertex[1] + outTangents[prevIndex][1],
          ],
          [
            currVertex[0] + inTangents[currIndex][0],
            currVertex[1] + inTangents[currIndex][1],
          ],
          [
            currVertex[0],
            currVertex[1],
          ],
        ],
      });
    } else if (tangentCount === 1) {
      if (hasTangent(outTangents[prevIndex])) {
        commands.push({
          command: 'Q',
          vertices: [
            [
              prevVertex[0] + outTangents[prevIndex][0],
              prevVertex[1] + outTangents[prevIndex][1],
            ],
            [
              currVertex[0],
              currVertex[1],
            ],
          ],
        });
      } else if (hasTangent(inTangents[currIndex])) {
        commands.push({
          command: 'Q',
          vertices: [
            [
              currVertex[0] + inTangents[currIndex][0],
              currVertex[1] + inTangents[currIndex][1],
            ],
            [
              currVertex[0],
              currVertex[1],
            ],
          ],
        });
      }
    } else if (tangentCount === 0) {
      commands.push({
        command: 'L',
        vertices: [
          currVertex,
        ],
      });
    }
  }

  return commands.map(command =>
    command.command + command.vertices.map(v => [v[0].toFixed(2), v[1].toFixed(2)]).join(',')
  );
}

/** Attempts to find a valid set of keyframes from ellipse size and position
    objects. */
function parseEllipseKeyframes(
  ellipseSize: Object,
  ellipsePosition: Object
): Array<{
    time: number,
    size: [number, number],
    position: [number, number]
  }> {

  const keyframes: Array<{
    time: number,
    size: ?[number, number],
    position: ?[number, number],
  }> = [];

  const sizeKeyframes: ?[Object] = ellipseSize['keyframes'];
  const positionKeyframes: ?[Object] = ellipsePosition['keyframes'];

  if (sizeKeyframes || positionKeyframes) {
    const keyframeMap: Object = {};

    if (sizeKeyframes) {
      sizeKeyframes.forEach(function (sizeKeyframe) {
        const time: number = sizeKeyframe['time'];
        const keyframe = keyframeMap[time];
        if (keyframe) {
          keyframe.size = sizeKeyframe['value'];
        } else {
          keyframeMap[time] = {size: sizeKeyframe['value']};
        }
      });
    }

    if (positionKeyframes) {
      positionKeyframes.forEach(function (positionKeyframe) {
        const time: number = positionKeyframe['time'];
        const keyframe = keyframeMap[time];
        if (keyframe) {
          keyframe.position = positionKeyframe['value'];
        } else {
          keyframeMap[time] = {position: positionKeyframe['value']};
        }
      });
    }

    const timeVals: Array<number> = Object.keys(keyframeMap).map(function (key) {
      return Number(key);
    });
    timeVals.sort();
    timeVals.forEach(function (time) {
      const keyframe = keyframeMap[time];
      if (keyframe) {
        keyframes.push({
          time: time,
          size: keyframe.size,
          position: keyframe.position,
        });
      }
    });

    let size: [number, number] =
      sizeKeyframes
      ? sizeKeyframes[0]['value']
      : ellipseSize['value'];
    let sizeIndex = -1;

    let position: [number, number] =
      positionKeyframes
      ? positionKeyframes[0]['value']
      : ellipsePosition['value'];
    let positionIndex = -1;

    // All properties must be specified for all keyframes. Iterate over the
    // keyframe array and fill in missing values using either the constant value
    // of the property or an interpolated value if keyframes are present.
    keyframes.forEach(function (keyframe, index) {
      if (keyframe.size) {
        sizeIndex = index;
        size = keyframe.size;
      } else {
        if (sizeKeyframes && sizeIndex  >= 0) {
          let nextSizeIndex: number = sizeIndex + 1;
          while (nextSizeIndex < keyframes.length) {
            if (keyframes[nextSizeIndex].size
                && keyframes[sizeIndex].size) {
              // Use linear interpolation
              const ratio: number =
                (keyframe.time - keyframes[sizeIndex].time)
                / (keyframes[nextSizeIndex].time - keyframes[sizeIndex].time);
              size =
                [
                  keyframes[sizeIndex].size[0]
                    + ratio * (keyframes[nextSizeIndex].size[0]
                                - keyframes[sizeIndex].size[0]),
                  keyframes[sizeIndex].size[1]
                    + ratio * (keyframes[nextSizeIndex].size[1]
                                - keyframes[sizeIndex].size[1]),
                ];
              console.warn('Linear interpolating ellipse size:', size, 'at time:', keyframe.time);
              break;
            }
            ++nextSizeIndex;
          }
        }

        keyframe.size = size;
      }

      if (keyframe.position) {
        positionIndex = index;
        position = keyframe.position;
      } else {
        if (positionKeyframes && positionIndex  >= 0) {
          let nextPositionIndex: number = positionIndex + 1;
          while (nextPositionIndex < keyframes.length) {
            if (keyframes[nextPositionIndex].position
                && keyframes[positionIndex].position) {
              // Use linear interpolation
              const ratio: number =
                (keyframe.time - keyframes[positionIndex].time)
                / (keyframes[nextPositionIndex].time - keyframes[positionIndex].time);
              position =
              [
                keyframes[positionIndex].position[0]
                  + ratio * (keyframes[nextPositionIndex].position[0]
                              - keyframes[positionIndex].position[0]),
                keyframes[positionIndex].position[1]
                  + ratio * (keyframes[nextPositionIndex].position[1]
                              - keyframes[positionIndex].position[1]),
              ];
              console.warn('Linear interpolating ellipse position:', position, 'at time:', keyframe.time);
              break;
            }
            ++nextPositionIndex;
          }
        }

        keyframe.position = position;
      }
    });
  }

  return keyframes.map(keyframe => {
    return {
      time: keyframe.time,
      size: keyframe.size ? keyframe.size : ellipseSize['value'],
      position: keyframe.position ? keyframe.position : ellipsePosition['value']
    }
  });
}

/** Creates an SVG-style command string for an ellipse defined by major- and
    minor-axis dimensions and center location. */
function makeEllipse(
  dimensions: [number, number],
  center: [number, number],
): string[] {

  // Draw the ellipse with four cubic bezier segments.
  // For an ellipse with semi-major axis 'a' and semi-minor axis 'b', the
  // segments are (in local coordinates):
  // (a, 0) -> (0, b)
  // (0, b) -> (-a, 0)
  // (-a, 0) -> (0, -b)
  // (0, -b) -> (a, 0)
  // The optimal length of the handle points to approximate a circle of radius
  // 1 with four cubic bezier segments is h = 0.551915024494.
  // Ref: http://spencermortensen.com/articles/bezier-circle/

  const major: number = 0.5 * dimensions[0];
  const minor: number = 0.5 * dimensions[1];
  const centerX: number = center[0];
  const centerY: number = center[1];

  const handle: number = 0.551915024494;
  const majorHandle: number = major * handle;
  const minorHandle: number = minor * handle;

  const commands: SVGCommand[] = [];

  // Move to (a, 0)
  commands.push({
    command: 'M',
    vertices: [[centerX + major, centerY]],
  });

  // Draw to (0, b)
  commands.push({
    command: 'C',
    vertices: [
      [
        centerX + major,
        centerY + minorHandle
      ],
      [
        centerX + majorHandle,
        centerY + minor
      ],
      [
        centerX,
        centerY + minor
      ],
    ],
  });

  // Draw to (-a, 0)
  commands.push({
    command: 'C',
    vertices: [
      [
        centerX - majorHandle,
        centerY + minor
      ],
      [
        centerX - major,
        centerY + minorHandle
      ],
      [
        centerX - major,
        centerY
      ],
    ],
  });

  // Draw to (0, -b)
  commands.push({
    command: 'C',
    vertices: [
      [
        centerX - major,
        centerY - minorHandle
      ],
      [
        centerX - majorHandle,
        centerY - minor
      ],
      [
        centerX,
        centerY - minor
      ],
    ],
  });

  // Draw to (a, 0)
  commands.push({
    command: 'C',
    vertices: [
      [
        centerX + majorHandle,
        centerY - minor
      ],
      [
        centerX + major,
        centerY - minorHandle
      ],
      [
        centerX + major,
        centerY
      ],
    ],
  });

  return commands.map(command =>
    command.command + command.vertices.map(
      v => [v[0].toFixed(2), v[1].toFixed(2)]
    ).join(',')
  );
}

/** Attempts to find a valid set of keyframes from rectangle size, position
    and roundness objects. */
function parseRectangleKeyframes(
  rectSize: Object,
  rectPosition: Object,
  rectRoundness: Object,
): Array<{
    time: number,
    size: [number, number],
    position: [number, number],
    roundness: ?number
  }> {

  const keyframes: Array<{
    time: number,
    size: ?[number, number],
    position: ?[number, number],
    roundness: ?number
  }> = [];

  const sizeKeyframes: ?[Object] = rectSize['keyframes'];
  const positionKeyframes: ?[Object] = rectPosition['keyframes'];
  const roundnessKeyframes: ?[Object] = rectRoundness['keyframes'];

  if (sizeKeyframes || positionKeyframes || roundnessKeyframes) {
    const keyframeMap: Object = {};

    if (sizeKeyframes) {
      sizeKeyframes.forEach(function (sizeKeyframe) {
        const time: number = sizeKeyframe['time'];
        const keyframe = keyframeMap[time];
        if (keyframe) {
          keyframe.size = sizeKeyframe['value'];
        } else {
          keyframeMap[time] = {size: sizeKeyframe['value']};
        }
      });
    }

    if (positionKeyframes) {
      positionKeyframes.forEach(function (positionKeyframe) {
        const time: number = positionKeyframe['time'];
        const keyframe = keyframeMap[time];
        if (keyframe) {
          keyframe.position = positionKeyframe['value'];
        } else {
          keyframeMap[time] = {position: positionKeyframe['value']};
        }
      });
    }

    if (roundnessKeyframes) {
      roundnessKeyframes.forEach(function (roundnessKeyframe) {
        const time: number = roundnessKeyframe['time'];
        const keyframe = keyframeMap[time];
        if (keyframe) {
          keyframe.roundness = roundnessKeyframe['value'];
        } else {
          keyframeMap[time] = {roundness: roundnessKeyframe['value']};
        }
      });
    }

    const timeVals: Array<number> = Object.keys(keyframeMap).map(function (key) {
      return Number(key);
    });
    timeVals.sort();
    timeVals.forEach(function (time) {
      const keyframe = keyframeMap[time];
      if (keyframe) {
        keyframes.push({
          time: time,
          size: keyframe.size,
          position: keyframe.position,
          roundness: keyframe.roundness,
        });
      }
    });

    let size: [number, number] =
      sizeKeyframes
      ? sizeKeyframes[0]['value']
      : rectSize['value'];
    let sizeIndex = -1;

    let position: [number, number] =
      positionKeyframes
      ? positionKeyframes[0]['value']
      : rectPosition['value'];
    let positionIndex = -1;

    let roundness: ?number =
      roundnessKeyframes
      ? roundnessKeyframes[0]['value']
      : (rectRoundness['value'] > 0 ? rectRoundness['value'] : null);
    let roundnessIndex: number = -1;

    // All properties must be specified for all keyframes. Iterate over the
    // keyframe array and fill in missing values using either the constant value
    // of the property or an interpolated value if keyframes are present.
    keyframes.forEach(function (keyframe, index) {
      if (keyframe.size) {
        sizeIndex = index;
        size = keyframe.size;
      } else {
        if (sizeKeyframes && sizeIndex  >= 0) {
          let nextSizeIndex: number = sizeIndex + 1;
          while (nextSizeIndex < keyframes.length) {
            if (keyframes[nextSizeIndex].size
                && keyframes[sizeIndex].size) {
              // Use linear interpolation
              const ratio: number =
                (keyframe.time - keyframes[sizeIndex].time)
                / (keyframes[nextSizeIndex].time - keyframes[sizeIndex].time);
              size =
                [
                  keyframes[sizeIndex].size[0]
                    + ratio * (keyframes[nextSizeIndex].size[0]
                                - keyframes[sizeIndex].size[0]),
                  keyframes[sizeIndex].size[1]
                    + ratio * (keyframes[nextSizeIndex].size[1]
                                - keyframes[sizeIndex].size[1]),
                ];
              console.warn('Linear interpolating rect size:', size, 'at time:', keyframe.time);
              break;
            }
            ++nextSizeIndex;
          }
        }

        keyframe.size = size;
      }

      if (keyframe.position) {
        positionIndex = index;
        position = keyframe.position;
      } else {
        if (positionKeyframes && positionIndex  >= 0) {
          let nextPositionIndex: number = positionIndex + 1;
          while (nextPositionIndex < keyframes.length) {
            if (keyframes[nextPositionIndex].position
                && keyframes[positionIndex].position) {
              // Use linear interpolation
              const ratio: number =
                (keyframe.time - keyframes[positionIndex].time)
                / (keyframes[nextPositionIndex].time - keyframes[positionIndex].time);
              position =
              [
                keyframes[positionIndex].position[0]
                  + ratio * (keyframes[nextPositionIndex].position[0]
                              - keyframes[positionIndex].position[0]),
                keyframes[positionIndex].position[1]
                  + ratio * (keyframes[nextPositionIndex].position[1]
                              - keyframes[positionIndex].position[1]),
              ];
              console.warn('Linear interpolating rect position:', position, 'at time:', keyframe.time);
              break;
            }
            ++nextPositionIndex;
          }
        }

        keyframe.position = position;
      }

      if (keyframe.roundness != null) {
        roundnessIndex = index;
        roundness = keyframe.roundness;
      } else {
        if (roundnessKeyframes && roundnessIndex  >= 0) {
          let nextRoundnessIndex: number = roundnessIndex + 1;
          while (nextRoundnessIndex < keyframes.length) {
            if (keyframes[nextRoundnessIndex].roundness != null
                && keyframes[roundnessIndex].roundness != null) {
              // Use linear interpolation
              const ratio: number =
                (keyframe.time - keyframes[roundnessIndex].time)
                / (keyframes[nextRoundnessIndex].time - keyframes[roundnessIndex].time);
              roundness =
                keyframes[roundnessIndex].roundness
                  + ratio * (keyframes[nextRoundnessIndex].roundness
                              - keyframes[roundnessIndex].roundness);
              console.warn('Linear interpolating rect roundness:', roundness, 'at time:', keyframe.time);
              break;
            }
            ++nextRoundnessIndex;
          }
        }

        keyframe.roundness = roundness;
      }
    });
  }

  return keyframes.map(keyframe => {
    return {
      time: keyframe.time,
      size: keyframe.size ? keyframe.size : rectSize['value'],
      position: keyframe.position ? keyframe.position : rectPosition['value'],
      roundness: keyframe.roundness != null ? keyframe.roundness : rectRoundness['value']
    }
  });
}

/** Creates an SVG-style command string for a rectangle defined by x and y
    dimensions, center location, and corner roundness. */
function makeRectangle(
  dimensions: [number, number],
  center: [number, number],
  roundness: ?number,
): string[] {

  const centerX: number = center[0];
  const centerY: number = center[1];
  const halfXDim: number = 0.5 * dimensions[0];
  const halfYDim: number = 0.5 * dimensions[1];

  // After Effects allows arbitrarily large roundness values, so we have to cap
  // it at the smaller half-side length of the rectangle.
  const minHalfSideLength: number = halfXDim < halfYDim ? halfXDim : halfYDim;
  let clippedRoundness: number = 0;
  if (roundness != null) {
    clippedRoundness = roundness > minHalfSideLength
      ? minHalfSideLength
      : roundness;
  }

  const commands: SVGCommand[] = [];

  // A rounded rectangle is drawn as 8 bezier segments; four quarter-circle
  // segments represented by cubic bezier curves for the corners, and four
  // straight-line linear segments for the edges. If roundess is not specified,
  // omit the corner segments.

  // Used to approximate a quarter-circle with a cubic bezier segment.
  // See the makeEllipse method for more info.
  const handle: number = 0.551915024494;

  // If roundness is zero, the corner segments will have zero length. This will
  // produce unpredictable results if the roundness is later animated to > 0.
  // To get around this, create non-zero length bezier handles facing the
  // correct direction This can happen for example when rectangle corner
  // roundness is animated from zero to non-zero.
  const handleToEdge: number =
    clippedRoundness > 0
    ? (1 - handle) * clippedRoundness
    : -1;

  // Move to upper right
  commands.push({
    command: 'M',
    vertices: [[centerX + halfXDim, centerY + halfYDim - clippedRoundness]],
  });

  if (roundness != null) {
    // Draw upper right corner
    commands.push({
      command: 'C',
      vertices: [
        [
          centerX + halfXDim,
          centerY + halfYDim - handleToEdge,
        ],
        [
          centerX + halfXDim - handleToEdge,
          centerY + halfYDim,
        ],
        [
          centerX + halfXDim - clippedRoundness,
          centerY + halfYDim
        ],
      ]
    });
  }

  // Draw upper edge
  commands.push({
    command: 'L',
    vertices: [
      [
        centerX - halfXDim + clippedRoundness,
        centerY + halfYDim
      ]
    ],
  });

  if (roundness != null) {
    // Draw upper left corner
    commands.push({
      command: 'C',
      vertices: [
        [
          centerX - halfXDim + handleToEdge,
          centerY + halfYDim,
        ],
        [
          centerX - halfXDim,
          centerY + halfYDim - handleToEdge,
        ],
        [
          centerX - halfXDim,
          centerY + halfYDim - clippedRoundness
        ],
      ]
    });
  }

  // Draw left egde
  commands.push({
    command: 'L',
    vertices: [
      [
        centerX - halfXDim,
        centerY - halfYDim + clippedRoundness
      ]
    ],
  });

  if (roundness != null) {
    // Draw lower left corner
    commands.push({
      command: 'C',
      vertices: [
        [
          centerX - halfXDim,
          centerY - halfYDim + handleToEdge,
        ],
        [
          centerX - halfXDim + handleToEdge,
          centerY - halfYDim,
        ],
        [
          centerX - halfXDim + clippedRoundness,
          centerY - halfYDim
        ],
      ]
    });
  }

  // Draw lower edge
  commands.push({
    command: 'L',
    vertices: [
      [
        centerX + halfXDim - clippedRoundness,
        centerY - halfYDim
      ]
    ],
  });

  if (roundness != null) {
    // Draw lower right corner
    commands.push({
      command: 'C',
      vertices: [
        [
          centerX + halfXDim - handleToEdge,
          centerY - halfYDim,
        ],
        [
          centerX + halfXDim,
          centerY - halfYDim + handleToEdge,
        ],
        [
          centerX + halfXDim,
          centerY - halfYDim + clippedRoundness
        ],
      ]
    });
  }

  // Draw right edge
  commands.push({
    command: 'L',
    vertices: [
      [
        centerX + halfXDim,
        centerY + halfYDim - clippedRoundness
      ]
    ],
  });

  return commands.map(command =>
    command.command + command.vertices.map(
      v => [v[0].toFixed(2), v[1].toFixed(2)]
    ).join(',')
  );
}

function parseTransformGroup(
  transformGroup: PropertyGroupTransformGroup,
  comp: CompItem,
  layer: AVLayer | ShapeLayer,
  kfDoc: KfDocument,
): KfProperty[] {
  let kfAnimGroupPropAnchorPointAnim: KfPropertyAnchorPoint;

  // initialize default position at (0, 0)
  let kfAnimGroupPropXPositionAnim: KfPropertyXPosition =
    {
      property: 'X_POSITION',
      key_values: [
        {
          start_frame: 0,
          data: [0],
        },
      ],
      timing_curves: [],
    };
  let kfAnimGroupPropYPositionAnim: KfPropertyYPosition =
    {
      property: 'Y_POSITION',
      key_values: [
        {
          start_frame: 0,
          data: [0],
        },
      ],
      timing_curves: [],
    };
  let kfAnimGroupPropRotationAnim: KfPropertyRotation;
  let kfAnimGroupPropScaleAnim: KfPropertyScale;
  let kfAnimGroupPropOpacityAnim: KfPropertyOpacity;

  const animations = [];

  transformGroup.properties.forEach(tfProp => {
    warnIfUsingMissingFeature(!!tfProp.expression, 'expression', tfProp, transformGroup, layer, comp);
    switch (tfProp.matchName) {

    case 'ADBE Anchor Point': {
      const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes, parseTimingFunctions);
      const key_values = keyValuesFor(comp, tfProp, (value: number[]) => [value[0], value[1]]);
      kfAnimGroupPropAnchorPointAnim = {property: 'ANCHOR_POINT', key_values, timing_curves};
    } break;

    case 'ADBE Position': {
      const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes, parseTimingFunctions);
      const x_key_values = keyValuesFor(comp, tfProp, (value: number[]) => [value[0]]);
      const y_key_values = keyValuesFor(comp, tfProp, (value: number[]) => [value[1]]);
      if (x_key_values.filter(({data:[value]}) => value !== 0).length > 0) {
        kfAnimGroupPropXPositionAnim = {property: 'X_POSITION', key_values: x_key_values, timing_curves};
      }
      if (y_key_values.filter(({data:[value]}) => value !== 0).length > 0) {
        kfAnimGroupPropYPositionAnim = {property: 'Y_POSITION', key_values: y_key_values, timing_curves};
      }
    } break;

    case 'ADBE Position_0': {
      const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes, parseTimingFunctions);
      const key_values = keyValuesFor(comp, tfProp, (value: number) => [value]);
      if (key_values.filter(({data:[value]}) => value !== 0).length > 0) {
        kfAnimGroupPropXPositionAnim = {property: 'X_POSITION', key_values, timing_curves};
      }
    } break;

    case 'ADBE Position_1': {
      const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes, parseTimingFunctions);
      const key_values = keyValuesFor(comp, tfProp, (value: number) => [value]);
      if (key_values.filter(({data:[value]}) => value !== 0).length > 0) {
        kfAnimGroupPropYPositionAnim = {property: 'Y_POSITION', key_values, timing_curves};
      }
    } break;

    case 'ADBE Scale': {
      const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes, parseTimingFunctions);
      const key_values = keyValuesFor(comp, tfProp, (value: number[]) => [value[0], value[1]]);
      if (key_values.filter(({data:[x, y]}) => x !== 100 || y !== 100).length > 0) {
        kfAnimGroupPropScaleAnim = {property: 'SCALE', key_values, timing_curves};
      }
    } break;

    case 'ADBE Opacity': {
      if (layer.matchName === 'ADBE Vector Layer' || isBitmapLayer(layer)) {
        const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes, parseTimingFunctions);
        const key_values = keyValuesFor(comp, tfProp, (value: number) => [value]);
        if (key_values.filter(({data:[value]}) => value !== 100).length > 0) {
          kfAnimGroupPropOpacityAnim = {property: 'OPACITY', key_values, timing_curves};
        }
      }
    } break;

    case 'ADBE Rotate Z': {
      const timing_curves = parseTimingFunctionsFromKeyframes(tfProp.keyframes, parseTimingFunctions);
      const key_values = keyValuesFor(comp, tfProp, (value: number) => [value]);
      if (key_values.filter(({data:[value]}) => value % 360 !== 0).length > 0) {
        kfAnimGroupPropRotationAnim = {property: 'ROTATION', key_values, timing_curves};
      }
    } break;

    default:
      warnIfUsingMissingFeature(tfProp.isModified, tfProp.__type__, tfProp, transformGroup, layer, comp);
    }
  });

  kfAnimGroupPropAnchorPointAnim && animations.push(kfAnimGroupPropAnchorPointAnim);
  kfAnimGroupPropXPositionAnim && animations.push(kfAnimGroupPropXPositionAnim);
  kfAnimGroupPropYPositionAnim && animations.push(kfAnimGroupPropYPositionAnim);
  kfAnimGroupPropScaleAnim && animations.push(kfAnimGroupPropScaleAnim);
  kfAnimGroupPropRotationAnim && animations.push(kfAnimGroupPropRotationAnim);
  kfAnimGroupPropOpacityAnim && animations.push(kfAnimGroupPropOpacityAnim);
  return animations;
}

function KfAnimationGroupFromAVLayer(
  comp: CompItem,
  layer: AVLayer,
  kfDoc: KfDocument,
): ?KfAnimationGroup {
  /*
    A KfAnimationGroup is a Null parent of a Shape or a Null
  */
  const kfAnimGroup: KfAnimationGroup = {
    group_id: layer.index,
    group_name: layer.name,
    animations: [],
  };
  if (layer.parent$index) {
    kfAnimGroup.parent_group = layer.parent$index;
  }

  layer.properties.forEach(layerProp => {
    switch (layerProp.matchName) {
    case 'ADBE Transform Group':
      kfAnimGroup.animations = parseTransformGroup(layerProp, comp, layer, kfDoc);
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

const parseTimingFunctionsFromKeyframes = (keyframes, mapping: Function) =>
  keyframes == null ? [] :
  keyframes.map((keyframe, index) =>
    mapping(keyframe, keyframes && keyframes[index + 1])
  ).filter(Boolean)
;

const parseShapeMorphTimingFunctions = (keyframeA, keyframeB) =>
  keyframeA && keyframeB &&
  [
    [0 + keyframeA.outTemporalEase[0].influence / 100, 0],
    [1 - keyframeB.inTemporalEase[0].influence / 100, 1],
  ]
;

const parseTimingFunctions = (keyframeA, keyframeB) => {
  if (!keyframeA || !keyframeB) {
    return null;
  }

  if (keyframeA.outInterpolationType === 'LINEAR' && keyframeB.inInterpolationType === 'LINEAR') {
    return [
      [1.0 / 3.0, 1.0 / 3.0],
      [2.0 / 3.0, 2.0 / 3.0],
    ];
  }

  if (keyframeA.outInterpolationType !== 'BEZIER' || keyframeB.inInterpolationType !== 'BEZIER') {
    console.error('UNSUPPORTED out interpolation type: \'' + keyframeA.outInterpolationType
      + '\' to in interpolation type: \'' + keyframeB.inInterpolationType + '\'');
  }

  const keyframeAOut = keyframeA.outTemporalEase[0];
  const keyframeBIn = keyframeB.inTemporalEase[0];
  const duration = keyframeB.time - keyframeA.time;
  let distance = 0;
  if (Array.isArray(keyframeA.value)) {
    distance = Math.sqrt((keyframeA.value[0] - keyframeB.value[0]) * (keyframeA.value[0] - keyframeB.value[0])
                       + (keyframeA.value[1] - keyframeB.value[1]) * (keyframeA.value[1] - keyframeB.value[1]));
  } else {
    distance = Math.abs(keyframeB.value - keyframeA.value);
  }

  let cp1Value = 0.0;
  let cp2Value = 1.0;

  if (distance !== 0) {
    cp1Value += duration / distance * Math.abs(keyframeAOut.speed) * (keyframeAOut.influence / 100);
    cp2Value -= duration / distance * Math.abs(keyframeBIn.speed) * (keyframeBIn.influence / 100);
  }
  const cp1 = [
    0.0 + (keyframeAOut.influence / 100),
    cp1Value,
  ];
  const cp2 = [
    1.0 - (keyframeBIn.influence / 100),
    cp2Value,
  ];

  return [cp1, cp2];
};

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
