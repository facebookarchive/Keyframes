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
  KfGradientStop,
  KfGradient,
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
  PropertyVectorAnchor,
  PropertyVectorPosition,
  PropertyVectorScale,
  PropertyVectorRotation,
  PropertyVectorGroupOpacity,
} from '../AfterEffectsTypes';

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
      if (layer['source$__type__'] === 'FootageItem' && layer['source$name'].endsWith('.png')) {
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
        kfDoc.features.unshift(shape);
      }
      break;
    default:
      console.warn(`Skipped unsupported layer type '${layer.__type__}'`);
    }
  });

  return kfDoc;
}

function commonFeatureFromLayer(
  comp: CompItem,
  layer: AVLayer | ShapeLayer,
  kfDoc: KfDocument,
): KfFeature {
  const kfFeature: KfFeature = {
    name: layer.name,
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
  kfFeature.backed_image = layer['source$name'].substr(0, layer['source$name'].length - '.png'.length);
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
    vectorFillColor,
    vectorStrokeColor,
    vectorStrokeWidth,
    vectorStrokeLineCap,
    vectorPosition,
    vectorScale,
    vectorRotation,
    vectorOpacity,
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
  }

  if (vectorFillColor) {
    // set fill color
    kfFeature.fill_color = getHexColorStringFromRGB(vectorFillColor.value);
  }
  if (vectorStrokeColor) {
    // set stroke color
    kfFeature.stroke_color = getHexColorStringFromRGB(vectorStrokeColor.value);
  }
  if (vectorStrokeWidth) {
    // set stroke width
    kfFeature.stroke_width = vectorStrokeWidth.value;
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
  vectorFillColor: ?PropertyVectorFillColor,
  vectorStrokeColor: ?PropertyVectorStrokeColor,
  vectorStrokeWidth: ?PropertyVectorStrokeWidth,
  vectorStrokeLineCap: ?PropertyVectorStrokeLineCap,
  vectorAnchor: ?PropertyVectorAnchor,
  vectorPosition: ?PropertyVectorPosition,
  vectorScale: ?PropertyVectorScale,
  vectorRotation: ?PropertyVectorRotation,
  vectorOpacity: ?PropertyVectorGroupOpacity,
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

  return {
    vectorShape,
    vectorFillColor,
    vectorStrokeColor,
    vectorStrokeWidth,
    vectorStrokeLineCap,
    vectorAnchor,
    vectorPosition,
    vectorScale,
    vectorRotation,
    vectorOpacity,
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
      if (layer.matchName === 'ADBE Vector Layer') {
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
    [0 + keyframeA.outTemporalEase[0].influence / 200, 0],
    [1 - keyframeB.inTemporalEase[0].influence / 200, 1],
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
  warnIfUsingMissingFeature(keyframeA.outInterpolationType !== 'BEZIER',
      'Unsupported interpolation method \'' + keyframeA.outInterpolationType + '\'');
  warnIfUsingMissingFeature(keyframeB.inInterpolationType !== 'BEZIER',
      'Unsupported interpolation method \'' + keyframeB.inInterpolationType + '\'');

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
  keyPath.unshift(comp && comp.parentFolder$name);
  console.warn('UNSUPPORTED: %s', keyPath.join(' → '));
}

module.exports = AECompToKeyframesAnimation;
