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
import {Surface, Group, Shape, Transform, LinearGradient, ClippingRectangle} from 'react-art';

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

class KfDrawable extends React.Component {
  props: {
    visible: boolean,
    doc: KfDocument,
    progress: number,
    width?: number, height?: number,
    x?: number, y?: number,
  };
  static defaultProps = {
    visible: true,
  };

  static _easingCache = new WeakMap();
  _easingForCurve(curve: KfTimingCurve): BezierEasing {
    let easing = KfDrawable._easingCache.get(curve);
    if (easing == null) {
      const [[curveA, curveB], [curveC, curveD]] = curve;
      easing = BezierEasing(curveA, curveB, curveC, curveD);
      KfDrawable._easingCache.set(curve, easing);
    }
    return easing;
  }

  static _tweenCache = new WeakMap();
  _tweenForCurve(curve: KfTimingCurve, a: string[], b: string[]): Morph.Tween {
    let tween = KfDrawable._tweenCache.get(curve);
    if (tween == null) {
      tween = Morph.Tween(
        Morph.Path(a.join(' ')),
        Morph.Path(b.join(' ')),
      );
      KfDrawable._tweenCache.set(curve, tween);
    }
    return tween;
  }

  static _gradientValuesCache = new WeakMap();
  _gradientNumberValuesFromStrings(values: KfValue<string>[]): KfValue<number[]>[] {
    let gradientValues = KfDrawable._gradientValuesCache.get(values);
    if (gradientValues == null) {
      gradientValues = values.map(prepGradientValuesForBlending);
      KfDrawable._gradientValuesCache.set(values, gradientValues);
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
    let {visible} = this.props;
    const {width, height, x, y, progress} = this.props;
    const {
      name,
      canvas_size: [docWidth, docHeight],
      features,
      animation_frame_count,
    } = this.props.doc;
    const currentFrameNumber = animation_frame_count * progress;
    let groupTransform = visible && (width || height) &&
      new Transform().scale(
        (width || height || docWidth) / docWidth,
        (height || width || docHeight) / docHeight,
      )
    ;
    if (x || y) {
      if (!groupTransform) {
        groupTransform = new Transform();
      }
      groupTransform.moveTo(x || 0, y || 0);
    }
    if (width === 0 || height === 0) {
      visible = false;
    }
    return (
      <Group key={name} visible={visible}
        width={docWidth} height={docHeight}
        transform={groupTransform}>
        {features.map((feature, index) => {
          const {name, fill_color, stroke_color, feature_animations, key_frames, timing_curves, effects} = feature;

          let fill;
          if (effects && effects.gradient) {
            const {gradient} = effects;
            switch (gradient.gradient_type) {
            case 'linear':
              const {color_start, color_end} = gradient;
              if (color_start && color_end) {
                // TODO(aylott): Gradient size should be sized to the shape, not the group!
                fill = new LinearGradient([
                  this.getGradientColor(color_start, currentFrameNumber),
                  this.getGradientColor(color_end, currentFrameNumber),
                ], 0, 0, 0, docHeight);
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
      </Group>
    );
  }
}

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

/*
class KfImageSurfacePrecomputed extends React.Component {
  props: {
    frameCount: number,
    Group: React.Component,
    progress: number,
    
  },
  render() {
    const {Group, frameCount, ...otherProps} = this.props;
    const frames = [];
    for (let frameIndex = 0; frameIndex < frameCount; frameIndex++) {
      frames[frameIndex] = <KfImageSurface {...otherProps} />
    }
    return (
      <Group>{frames}</Group>
    );
  }
}
*/

class KfImageSurface extends React.Component {
  props: {
    doc: KfDocument,
    progress: number,
    width?: number,
    height?: number,
  };
  render() {
    const {
      width, height,
      doc: {
        canvas_size: [docWidth, docHeight],
      },
    } = this.props;
    return (
      <Surface
        width={width || height || docWidth}
        height={height || width || docHeight}>
        <KfDrawable {...this.props} />
      </Surface>
    );
  }
}

class KfSpriteMapSurfaceAnimator extends React.Component {
  props: {
    progress: number,
    doc: KfDocument,
    width?: number,
    height?: number,
    style?: {[key:string]: any},
  };
  render() {
    const {progress, ...otherProps} = this.props;
    const {
      style,
      width, height,
      doc: {
        canvas_size: [docWidth, docHeight],
        animation_frame_count,
      },
    } = this.props;

    const finalWidth = width || height || docWidth;
    const finalHeight = height || width || docHeight;
    const frameCount = animation_frame_count;
    const frameIndexMax = frameCount - 1;
    const cols = Math.ceil(Math.sqrt(frameCount));
    const rows = Math.ceil(frameCount / cols);
    const frameIndex = Math.round(frameIndexMax * progress);
    const currentRow = Math.floor(frameIndex / cols);
    const currentCol = frameIndex % cols;
    const x = finalWidth * currentCol;
    const y = finalHeight * currentRow;

    return (
      <div style={{...style, width: finalWidth, height: finalHeight, display:'inline-block', position:'relative', overflow:'hidden', transform: `translate3d(0,0,0)`}}>
        <div style={{position:'absolute', /*left: -x, top: -y,*/ transform: `translateX(${-x}px) translateY(${-y}px)`}}>
          <KfSpriteMapSurface frameNum={frameIndex+1} frameIndex={frameIndex} currentRow={currentRow} currentCol={currentCol}
          {...otherProps} />
        </div>
      </div>
    );
  }
}

class KfSpriteMapSurface extends React.Component {
  props: {
    doc: KfDocument,
    width?: number,
    height?: number,
  };
  shouldComponentUpdate({doc: docB, width: widthB, height: heightB}): boolean {
    const {doc: docA, width: widthA, height: heightA} = this.props;
    return !(
      docA === docB &&
      widthA === widthB &&
      heightA === heightB
    );
  }
  render() {
    const {
      width, height,
      doc: {
        canvas_size: [docWidth, docHeight],
        animation_frame_count,
      },
    } = this.props;
    
    const finalWidth = width || height || docWidth;
    const finalHeight = height || width || docHeight;
    const frameCount = animation_frame_count;
    const cols = Math.ceil(Math.sqrt(frameCount));
    const rows = Math.ceil(frameCount / cols);
    const frames = new Array(frameCount);
    let frameIndex = -1;
    let currentRow = 0;
    let currentCol = 0;

    while (++frameIndex < frameCount) {
      const x = finalWidth * currentCol;
      const y = finalHeight * currentRow;

      frames[frameIndex] = (
        <KfDrawable key={frameIndex} {...this.props} progress={frameIndex / frameCount} x={x} y={y} />
      );

      currentCol ++;
      if (currentCol >= cols) {
        currentRow ++;
        currentCol = 0;
      }
      if (currentRow > rows) {
        console.warn("sprite map not large enough :'(");
      }
    }

    return (
      <Surface
        width={finalWidth * cols}
        height={finalHeight * rows}>
        {frames}
      </Surface>
    );
  }
}

class KfDemo extends React.Component {
  props: {
    fps: number,
    duration: number,
    renderWithProgressAndSize: (progress: number, size:number) => React.Element<*>,
  };
  state = {
    progress: 0,
    animating: false,
    size: 64,
  };

  _rafTimer: ?number;
  animationStartTime: ?number;
  animationEndTime: ?number;
  nextFrameStartTime: ?number;
  handleRAF = () => {
    this._rafTimer = null;
    if (!this.state.animating) {
      return false;
    }
    const now = Date.now();
    if (!(this.nextFrameStartTime && this.animationEndTime)) {
      this.nextFrameStartTime = now;
      this.animationEndTime = now + this.props.duration;
    }
    if (!(now < this.nextFrameStartTime)) {
      let {progress} = this.state;
      if (+progress !== +progress) {
        progress = 0;
      }
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
      if (progress !== progress) {
        console.error('progress NaN');
        debugger;
      }
      
      if (progress > 1) {
        progress = 0;
      }
      this.setState({progress});
    }
    this.tickRAF();
  }

  tickRAF() {
    this._rafTimer && window.cancelAnimationFrame(this._rafTimer);
    this._rafTimer = window.requestAnimationFrame(this.handleRAF);
  }
  componentDidMount() {
    this.tickRAF();
  }
  componentWillUnmount() {
    this._rafTimer && window.cancelAnimationFrame(this._rafTimer);
  }
  componentWillUpdate(nextProps, {animating}) {
    if (animating && !this.state.animating) {
      this.tickRAF();
    }
  }

  render() {
    const {progress, animating, size} = this.state;
    return (
      <div>
        <table style={{width:'100%'}}><tbody>
          <tr><th style={{width:1}}>Size</th>
            <td>
              <input style={{width:'100%'}} type="range" value={size} min={16} max={512} step={4} onChange={
                ({target:{valueAsNumber:size}}) => this.setState({size})
              } />
            </td>
            <td style={{width:1}}>
              <input style={{width:'8ex'}} type="number" value={size} min={16} max={512} step={4} onChange={
                ({target:{valueAsNumber:size}}) => this.setState({size})
              } />
            </td>
          </tr>
          {
            this.props.hasProgress && (
              <tr><th style={{width:1}}>Progress</th>
                <td>
                  <input style={{width:'100%'}} type="range" value={progress} min={0} max={1} step={1/9999} onChange={
                    ({target:{valueAsNumber:progress}}) => this.setState({progress})
                  } />
                </td>
                <td style={{width:1}}>
                  <input style={{width:'8ex'}} type="number" value={progress} min={0} max={1} step={1/9999} onChange={
                    ({target:{valueAsNumber:progress}}) => this.setState({progress})
                  } />
                </td>
              </tr>
            )
          }
          </tbody></table>
        <br />
        {
          this.props.hasProgress && (
            <label><b>Animating?</b> <input type="checkbox" checked={animating} onChange={
              ({target:{checked: animating}}) => this.setState({animating})
            } /></label>
          )
        }
        <br />
        {(this.props.shouldRender || this.state.shouldRenderStuff) && this.props.renderWithProgressAndSize(progress, size) || (
          <div>
            <button onClick={e => this.setState({shouldRenderStuff:true})}>Initialize</button>
          </div>
        )}
      </div>
    );
  }
}

document.write(`
  <div id=KfDemoRoot>
    <button onclick="window.StartKfDemo()">Start!</button>
  </div>
`);

window.StartKfDemo = () => {
  ReactDOM.render(
    <div>
    
    
      <h2>SpriteMapped fps capped</h2>
      <KfDemo hasProgress fps={24} duration={4000} renderWithProgressAndSize={
        (progress, size) => (
          <div>
            <KfSpriteMapSurfaceAnimator width={size} progress={progress} doc={require('./assets/sorry.json')} />
            <KfSpriteMapSurfaceAnimator width={size} progress={progress} doc={require('./assets/anger.json')} />
            <KfSpriteMapSurfaceAnimator width={size} progress={progress} doc={require('./assets/haha.json')} />
            <KfSpriteMapSurfaceAnimator width={size} progress={progress} doc={require('./assets/like.json')} />
            <KfSpriteMapSurfaceAnimator width={size} progress={progress} doc={require('./assets/yay.json')} />
            <KfSpriteMapSurfaceAnimator width={size} progress={progress} doc={require('./assets/love.json')} />
          </div>
        )
      } />
      
      <hr />
      
      <h2>Render on demand</h2>
      <KfDemo hasProgress fps={24} duration={4000} renderWithProgressAndSize={
        (progress, size) => (
          <div>
            <KfImageSurface width={size} progress={progress} doc={require('./assets/sorry.json')} />
            <KfImageSurface width={size} progress={progress} doc={require('./assets/anger.json')} />
            <KfImageSurface width={size} progress={progress} doc={require('./assets/haha.json')} />
            <KfImageSurface width={size} progress={progress} doc={require('./assets/like.json')} />
            <KfImageSurface width={size} progress={progress} doc={require('./assets/yay.json')} />
            <KfImageSurface width={size} progress={progress} doc={require('./assets/love.json')} />
          </div>
        )
      } />
      <hr />
      
      <h2>SpriteMap</h2>
      <KfDemo fps={24} duration={4000} renderWithProgressAndSize={
        (progress, size) => (
          <div>
            <KfSpriteMapSurface width={size} doc={require('./assets/sorry.json')} />
            <KfSpriteMapSurface width={size} doc={require('./assets/anger.json')} />
            <KfSpriteMapSurface width={size} doc={require('./assets/haha.json')} />
            <KfSpriteMapSurface width={size} doc={require('./assets/like.json')} />
            <KfSpriteMapSurface width={size} doc={require('./assets/yay.json')} />
            <KfSpriteMapSurface width={size} doc={require('./assets/love.json')} />
          </div>
        )
      } />
    </div>,
    document.getElementById('KfDemoRoot')
  );
}

window.StartKfDemo();

// setTimeout(window.StartKfDemo, 10);
