/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 */
'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import {Surface, Group, Shape, Transform, LinearGradient} from 'react-art';

import BezierEasing from 'bezier-easing';
import Morph from 'art/morph/path';
import Color from 'art/core/color';

// require('art/modes/current').setCurrent(require('art/modes/dom'));

type KfDocument = {
  key: number,
  name: string,
  canvas_size: KfPoint,
  frame_rate: number,
  animation_frame_count: number,
  features: KfFeature[],
  animation_groups: KfAnimationGroup[],
};

type KfAnimationGroup = {
  group_id: number,
  group_name: string,
  parent_group?: number,
  animations: KfProperty[],
};

type KfFeature = {
  name: string,
  fill_color?: string,
  stroke_color?: string,
  stroke_width?: number,
  effects?: {
    gradient?: KfGradient,
  },
  animation_group?: number,
  feature_animations?: KfProperty[],

  timing_curves?: KfTimingCurve[],
  key_frames: KfValue<string[]>[],
}

type KfValue<T> = {
  start_frame: number,
  data: T,
};

type KfAnimatable<T> = {
  timing_curves: KfTimingCurve[],
  key_values: KfValue<T>[],
};

type KfPoint = [number, number];

type KfProperty = KfPropertyPosition | KfPropertyRotation | KfPropertyScale | KfPropertyStrokeWidth;

type KfPropertyPosition = KfAnimatable<KfPoint> & {
  property: 'POSITION',
  anchor?: KfPoint,
};
type KfPropertyRotation = KfAnimatable<[number] | [number, number, number]> & {
  property: 'ROTATION',
  anchor: KfPoint,
};
type KfPropertyScale = KfAnimatable<KfPoint> & {
  property: 'SCALE',
  anchor?: KfPoint,
};
type KfPropertyStrokeWidth = KfAnimatable<[number]> & {
  property: 'STROKE_WIDTH',
};

type KfTimingCurve = [KfPoint, KfPoint];

type KfGradientStop = KfAnimatable<string>;

type KfGradient = {
  gradient_type: 'linear' | 'radial',
  color_start?: KfGradientStop,
  color_end?: KfGradientStop,
  ramp_start?: KfGradientStop,
  ramp_end?: KfGradientStop,
};

const defaultProps = {
  progress: 0,
};

class KfImage extends React.Component {

  static defaultProps: typeof defaultProps;
  props: {
    doc: KfDocument,
  } & typeof defaultProps;

  _easingCache = new WeakMap();
  _easingForCurve(curve: KfTimingCurve): BezierEasing {
    let easing = this._easingCache.get(curve);
    if (easing == null) {
      const [[curveA, curveB], [curveC, curveD]] = curve;
      easing = BezierEasing(curveA, curveB, curveC, curveD);
      this._easingCache.set(curve, easing);
    }
    return easing;
  }

  _tweenCache = new WeakMap();
  _tweenForCurve(curve: KfTimingCurve, a: string[], b: string[]): Morph.Tween {
    let tween = this._tweenCache.get(curve);
    if (tween == null) {
      tween = Morph.Tween(
        Morph.Path(a.join(' ')),
        Morph.Path(b.join(' ')),
      );
      this._tweenCache.set(curve, tween);
    }
    return tween;
  }

  _gradientValuesCache = new WeakMap();
  _gradientNumberValuesFromStrings(values: KfValue<string>[]): KfValue<number[]>[] {
    let gradientValues = this._gradientValuesCache.get(values);
    if (gradientValues == null) {
      gradientValues = values.map(prepGradientValuesForBlending);
      this._gradientValuesCache.set(values, gradientValues);
    }
    return gradientValues;
  }

  blendShapes = (a: string[], b: string[], curve: KfTimingCurve, progress: number): any => {
    const easing = this._easingForCurve(curve);
    const tween = this._tweenForCurve(curve, a, b);
    tween.tween(easing(progress));
    return tween;
  }
  blendNumbers = (aNums: number[], bNums: number[], curve: KfTimingCurve, progress: number): number[] => {
    const easing = this._easingForCurve(curve);
    const easedProgress = easing(progress);
    const blendedNums = new Array(aNums.length);
    for (let index = aNums.length; --index >= 0;){
      blendedNums[index] = blendNumbersLinear(aNums[index], bNums[index], easedProgress);
    }
    return blendedNums;
  }

  getGradientColor({key_values, timing_curves}: KfGradientStop, currentFrameNumber: number): Color {
    const values = this._gradientNumberValuesFromStrings(key_values);
    const colorParts = getValueForCurrentFrame(values, timing_curves, currentFrameNumber, this.blendNumbers) || [];
    const [alpha, red, green, blue] = colorParts;
    return { alpha: Math.round(alpha), red: Math.round(red), green: Math.round(green), blue: Math.round(blue), isColor: true };
  }

  render() {
    const {progress} = this.props;
    const {
      name,
      canvas_size: [canvasWidth, canvasHeight],
      features,
      animation_frame_count,
    } = this.props.doc;
    const currentFrameNumber = animation_frame_count * progress;
    return (
      <Surface key={name} width={canvasWidth} height={canvasHeight}>
        {features.map((feature, index) => {
          const {name, fill_color, stroke_color, feature_animations, key_frames, timing_curves, effects} = feature;

          let fill;
          if (effects && effects.gradient) {
            const {gradient} = effects;
            switch (gradient.gradient_type) {
            case 'linear':
              const {color_start, color_end} = gradient;
              if (color_start && color_end) {
                // TODO(aylott): Gradient size should be sized to the shape, not the canvas!
                fill = new LinearGradient([
                  this.getGradientColor(color_start, currentFrameNumber),
                  this.getGradientColor(color_end, currentFrameNumber),
                ], 0, 0, 0, canvasHeight);
              }
              break;
            // TODO(aylott): Support radial gradient_type
            default:
              console.warn(`Skipping unsupported gradient_type ${gradient.gradient_type}`);
            }
          }

          const shapeData = getValueForCurrentFrame(key_frames, timing_curves, currentFrameNumber, this.blendShapes);

          let {stroke_width} = feature;
          feature_animations && feature_animations.filter(filterToStroke).forEach(({property, timing_curves, key_values}) => {
            const values: ?number[] = getValueForCurrentFrame(key_values, timing_curves, currentFrameNumber, this.blendNumbers);
            if (values) {
              stroke_width = values[0];
            }
          });

          let transform = feature_animations && transformFromAnimations(feature_animations, currentFrameNumber, this.blendNumbers);
          if (feature.animation_group) {
            const groupTransform = transformUsingAnimationGroups(this.props.doc, feature.animation_group, currentFrameNumber, this.blendNumbers);
            if (transform) {
              transform = groupTransform.transform(transform);
            } else {
              transform = groupTransform;
            }
          }

          const shapeElement = shapeData && (
            <Shape key={name}
              fill={fill || hexColorSwapAlphaPosition(fill_color)}
              stroke={hexColorSwapAlphaPosition(stroke_color)}
              strokeWidth={stroke_width}
              d={shapeData.join && shapeData.join(' ') || shapeData}
              transform={transform}
            />
          );
          
          return shapeElement;
        })}
      </Surface>
    );
  }
}

KfImage.defaultProps = defaultProps;

const filterToStroke = ({property}) => property === 'STROKE_WIDTH';

const prepGradientValuesForBlending = (
  {start_frame, data}: KfValue<string>
): KfValue<number[]> => (
  {start_frame, data: Color.parseHEX(data)}
);

function blendNumbersLinear(aNum: number, bNum: number, progress: number): number {
  const delta = bNum - aNum;
  return delta * progress + aNum;
}

if (typeof __DEV__ !== 'undefined' && __DEV__) {
  function testEqual(testEqualFn, expected){
    const result = testEqualFn();
    if (!(result === expected)) {
      console.error({testEqualFn, result, expected});
    }
  }

  testEqual(() => blendNumbersLinear(0, 1, 0), 0);
  testEqual(() => blendNumbersLinear(0, 1, 1), 1);
  testEqual(() => blendNumbersLinear(0, 1, 0.5), 0.5);
  testEqual(() => blendNumbersLinear(-99, 99, 0.5), 0);
  testEqual(() => blendNumbersLinear(-99, 99, 0.75), 49.5);
}

function filterGroupsByThisId({group_id}: KfAnimationGroup): boolean {
  const targetID: number = this;
  return group_id === targetID;
}

function transformUsingAnimationGroups<T>(
  doc: KfDocument,
  id: number,
  currentFrameNumber: number,
  blend?: (a:number[], b:number[], curve: KfTimingCurve, progress: number) => number[],
): Transform {
  const group = doc.animation_groups.filter(filterGroupsByThisId, id)[0];
  if (!group) {
    throw new Error(`Animation Group ${id} not found`);
  }
  const {animations, parent_group} = group;
  let transform = transformFromAnimations(animations, currentFrameNumber, blend);
  if (parent_group) {
    const groupTransform = transformUsingAnimationGroups(doc, parent_group, currentFrameNumber, blend);
    transform = groupTransform.transform(transform);
  }
  return transform;
}


function transformFromAnimations(
  animations: KfProperty[],
  currentFrameNumber: number,
  blend?: (a:number[], b:number[], curve: KfTimingCurve, progress: number) => number[],
): ?Transform {
  if (!(animations && animations.length > 0)) {
    return;
  }
  const transform = new Transform();
  animations.sort(sortAnimations).forEach((anim: KfProperty) => {
    const {property, timing_curves, key_values} = anim;
    const {anchor} = (anim: any);
    const values: ?number[] = getValueForCurrentFrame(key_values, timing_curves, currentFrameNumber, blend);
    if (values == null) {
      return;
    }
    switch (anim.property) {
    case 'STROKE_WIDTH':
      break;
    case 'POSITION':
      if (anchor) {
        transform.translate(-anchor[0], -anchor[1]);
      } else {
        const defaultAnchor = key_values[0].data;
        transform.translate(-defaultAnchor[0], -defaultAnchor[1]);
      }
      transform.translate(values[0], values[1]);
      break;
    case 'SCALE':
      anchor && transform.translate(anchor[0], anchor[1]);
      transform.scale(values[0] / 100, values[1] / 100);
      anchor && transform.translate(-anchor[0], -anchor[1]);
      break;
    case 'ROTATION':
      if (!anchor) {
        console.warn(`Skipping ${property} because anchor is missing`);
      } else {
        transform.rotate(values[0], anchor[0], anchor[1]);
      }
      break;
    default:
      console.warn('Skipping unsupported property', property);
    }
  });
  return transform;
}

const AnimationOrder = [
  'STROKE_WIDTH',
  'POSITION',
  'SCALE',
  'ROTATION',
];

function sortAnimations({property:aProp}: KfProperty, {property:bProp}: KfProperty): number {
  const a = AnimationOrder.indexOf(aProp);
  const b = AnimationOrder.indexOf(bProp);
  return a > b
    ? 1
    : b > a
      ? -1
      : 0
  ;
}

function hexColorSwapAlphaPosition(color: ?string): ?string {
  if (!color) {
    return;
  }
  return '#' + color.substr(3,6) + color.substr(1,2);
}

function getValueForCurrentFrame<T>(
  kfValues: KfValue<T>[],
  timing_curves?: KfTimingCurve[],
  targetFrame: number,
  blend?: (a:T, b:T, curve: KfTimingCurve, progress: number) => T,
): ?T {
  let kfValueIndex, kfValue, kfValueNext;
  if (kfValues.length > 0) {
    let _kfValue, targetFrameBestMatch = -1;
    for (let index = -1, kfValuesCount = kfValues.length; ++index < kfValuesCount;) {
      _kfValue = kfValues[index]
      const {start_frame} = _kfValue;
      // There can't be more than one perfect match
      if (start_frame === targetFrame) {
        kfValue = _kfValue;
        kfValueIndex = index;
        kfValueNext = null;
        break;
      }
      // Skip any that happen later than now
      if (start_frame > targetFrame) {
        continue;
      }
      // Keep any that are closer to the target
      if (start_frame >= targetFrameBestMatch) {
        targetFrameBestMatch = start_frame;
        kfValue = _kfValue;
        kfValueIndex = index;
        kfValueNext = kfValues[index + 1];
      }
    }
  }
  if (!kfValue) {
    kfValueIndex = 0;
    kfValue = kfValues[kfValueIndex];
  }
  if (kfValue && kfValueNext && timing_curves && blend && kfValueIndex != null) {
    const minFrame = kfValue.start_frame;
    const maxFrame = kfValueNext.start_frame;
    const progressBetweenFrames = (targetFrame - minFrame) / (maxFrame - minFrame);
    const curve = timing_curves[kfValueIndex];
    return blend(kfValue.data, kfValueNext.data, curve, progressBetweenFrames);
  }
  return kfValue && kfValue.data || null;
}


function mapValueInRange(value: number, fromLow: number, fromHigh: number, toLow: number, toHigh: number): number {
  const fromRangeSize = fromHigh - fromLow;
  const toRangeSize = toHigh - toLow;
  const valueScale = (value - fromLow) / fromRangeSize;
  return toLow + (valueScale * toRangeSize);
}

class KfDemo extends React.Component {
  props: {
    fps: number,
    duration: number,
  };
  state: {
    progress: number,
    animating: boolean
  };

  constructor(...args) {
    super(...args);
    this.state = {
      progress: 0,
      animating: false,
    };
  }

  _rafTimer: ?number;
  animationStartTime: number;
  animationEndTime: number;
  nextFrameStartTime: number;
  handleRAF = () => {
    this._rafTimer = null;
    if (!this.state.animating) {
      return false;
    }
    const now = Date.now();
    if (!(now < this.nextFrameStartTime)) {
      let {progress} = this.state;
      if (progress === 0) {
        this.animationStartTime = now;
        this.animationEndTime = now + this.props.duration;
      }
      const frameTime = 1000 / this.props.fps;
      this.nextFrameStartTime = now + frameTime;
      progress = mapValueInRange(
        this.nextFrameStartTime,// value
        this.animationStartTime,// fromLow
        this.animationEndTime,// fromHigh
        0,// toLow
        1,// toHigh
      );
      
      if (progress > 1) {
        progress = 0;
      }
      this.setState({progress});
    }
    this.tickRAF();
  }

  tickRAF() {
    if (this._rafTimer) {
      return;
    }
    this._rafTimer = window.requestAnimationFrame(this.handleRAF);
  }
  componentDidMount() {
    this.tickRAF();
  }
  componentWillUnmount() {
    window.cancelAnimationFrame(this._rafTimer);
  }
  componentWillUpdate(nextProps, nextState) {
    this.tickRAF();
  }

  render() {
    const {progress, animating} = this.state;
    return (
      <div>
        <input style={{width:'100%'}} type="range" value={progress} min={0} max={1} step={1/9999} onChange={
          ({target:{valueAsNumber:progress}}) => this.setState({progress})
        } />
        <br />
        <label>Animating? <input type="checkbox" checked={animating} onChange={
          ({target:{checked: animating}}) => this.setState({animating})
        } /></label>
        <br />
        <KfImage progress={progress} doc={require('./assets/sorry.json')} />
        <KfImage progress={progress} doc={require('./assets/anger.json')} />
        <KfImage progress={progress} doc={require('./assets/haha.json')} />
        <KfImage progress={progress} doc={require('./assets/like.json')} />
        <KfImage progress={progress} doc={require('./assets/yay.json')} />
        <KfImage progress={progress} doc={require('./assets/love.json')} />
      </div>
    );
  }
}

document.write('<div id=KfDemoRoot></div>');

ReactDOM.render(
  <div>
    <KfDemo fps={24} duration={4000} />
  </div>,
  document.getElementById('KfDemoRoot')
);
