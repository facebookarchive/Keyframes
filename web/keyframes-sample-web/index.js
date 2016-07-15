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
import {Surface, Group, Shape, Transform} from 'react-art';

import BezierEasing from 'bezier-easing';
import Morph from 'art/morph/path';

require('art/modes/current').setCurrent(require('art/modes/dom'));

type KfDocument = {
  key: number,
  name: string,
  canvas_size: KfPoint,
  frame_rate: number,
  animation_frame_count: number,
  features: Array<KfFeature>,
  animation_groups: Array<KfAnimationGroup>,
};

type KfAnimationGroup = {
  group_id: number,
  group_name: string,
  parent_group?: number,
  animations: Array<KfProperty>,
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
  feature_animations?: Array<KfProperty>,

  timing_curves?: Array<KfTimingCurve>,
  key_frames: Array<KfValue<Array<string>>>,
}

type KfValue<T> = {
  start_frame: number,
  data: T,
};

type KfAnimatable<T> = {
  timing_curves: Array<KfTimingCurve>,
  key_values: Array<KfValue<T>>,
};

type KfPoint = [number, number];

type KfProperty = KfPropertyPosition | KfPropertyRotation | KfPropertyScale | KfPropertyStrokeWidth;

type KfPropertyPosition = KfAnimatable<KfPoint> & {
  property: 'POSITION',
  anchor?: KfPoint,
};
type KfPropertyRotation = KfAnimatable<[number] | [number, number, number]> & {
  property: 'ROTATION',
  anchor?: KfPoint,
};
type KfPropertyScale = KfAnimatable<[number, number]> & {
  property: 'SCALE',
  anchor?: KfPoint,
};
type KfPropertyStrokeWidth = KfAnimatable<[number]> & {
  property: 'STROKE_WIDTH',
};

type KfTimingCurve = [KfPoint, KfPoint];

type KfGradientStop = KfAnimatable<KfValue<string>>;

type KfGradient = {
  gradient_type: 'linear' | 'radial',
  color_start?: KfGradientStop,
  color_end?: KfGradientStop,
  ramp_start?: KfGradientStop,
  ramp_end?: KfGradientStop,
};

const defaultProps = {
  loop: false,
  progress: 0,
  currentFrameNumber: 0,
};

class KfSample extends React.Component {
  state: {
    currentFrameNumber: number,
  };

  static defaultProps: typeof defaultProps;
  props: {
    doc: KfDocument,
  } & typeof defaultProps;

  constructor(props, ...args) {
    super(props, ...args);
    const {currentFrameNumber} = props;
    this.state = {currentFrameNumber};
    this.handleRAF = this.handleRAF.bind(this);
    console.log(props);
  }

  componentWillReceiveProps(nextProps) {
    const {currentFrameNumber} = nextProps;
    this.setState({currentFrameNumber});
  }

  nextFrameStartTime: number;
  _rafTimer: number;
  handleRAF: Function;
  handleRAF() {
    const now = Date.now();
    if (!(now < this.nextFrameStartTime)) {
      let currentFrameNumber = this.state.currentFrameNumber || 0;
      currentFrameNumber ++;
      const {frame_rate, animation_frame_count} = this.props.doc;
      const frameTime = 1000 / frame_rate
      this.nextFrameStartTime = now + frameTime;
      
      if (currentFrameNumber > animation_frame_count) {
        if (!this.props.loop) {
          return;
        }
        currentFrameNumber = 0;
      }
      this.setState({currentFrameNumber});
    }
    this.tickRAF();
  }

  tickRAF() {
    this._rafTimer = window.requestAnimationFrame(this.handleRAF);
  }
  componentDidMount() {
    this.tickRAF();
  }
  componentWillUnmount() {
    window.cancelAnimationFrame(this._rafTimer);
  }

  // blendShapesCache: {[key:string]:{}};
  // blendShapes: (a: Path, b: Path, curve: KfTimingCurve, progress: number) => Path;
  blendShapes(a: Array<string>, b: Array<string>, curve: KfTimingCurve, progress: number): string {
    const aPath = Morph.Path(a.join(' '));
    const bPath = Morph.Path(b.join(' '));
    const tween = Morph.Tween(aPath, bPath);
    tween.tween(progress);
    return tween;
  }

  render() {
    // console.log(this.props.doc);
    const {
      name,
      canvas_size: [canvasWidth, canvasHeight],
      features,
    } = this.props.doc;
    const {currentFrameNumber} = this.state;
    // console.log({name, currentFrameNumber})
    return (
      <Surface key={name} width={canvasWidth} height={canvasHeight}>
        {features.map((feature, index) => {
          const {name, fill_color, stroke_color, stroke_width} = feature;
          const shapeData = getValueForCurrentFrame(feature.key_frames, feature.timing_curves, currentFrameNumber, this.blendShapes);
          // new ReactART.LinearGradient(["black", "white"])
          return shapeData && (
            <Shape key={name}
              fill={hexColorSwapAlphaPosition(fill_color)}
              stroke={hexColorSwapAlphaPosition(stroke_color)}
              strokeWidth={stroke_width}
              d={shapeData.join && shapeData.join(' ') || shapeData}
            />
          );
        })}
      </Surface>
    );
  }
}

KfSample.defaultProps = defaultProps;


// function convertKfDocToNestedLayers(kfDoc: KfDocument): KfArtTree {
//
// }


function hexColorSwapAlphaPosition(color: ?string): ?string {
  if (!color) {
    return;
  }
  return '#' + color.substr(3,6) + color.substr(1,2);
}

function getValueForCurrentFrame<T>(
  kfValues: Array<KfValue<T>>,
  timing_curves?: Array<KfTimingCurve>,
  targetFrame: number,
  blend?: (a:T, b:T) => T,
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
    kfValueNext = kfValues[kfValueIndex + 1];
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


class KfDemo extends React.Component {
  state: {currentFrameNumber:number};
  constructor(...args) {
    super(...args);
    this.state = {currentFrameNumber:0};
  }
  render() {
    const {currentFrameNumber} = this.state;
    return (
      <div>
        <KfSample currentFrameNumber={currentFrameNumber} doc={require('./assets/sample_anger_temp.json')} />
        <KfSample currentFrameNumber={currentFrameNumber} doc={require('./assets/anger.json')} />
        <KfSample currentFrameNumber={currentFrameNumber} doc={require('./assets/sorry.json')} />
        <KfSample currentFrameNumber={currentFrameNumber} doc={require('./assets/sample_haha.json')} />
        <KfSample currentFrameNumber={currentFrameNumber} doc={require('./assets/sample_like.json')} />
        <KfSample currentFrameNumber={currentFrameNumber} doc={require('./assets/yay.json')} />
        <KfSample currentFrameNumber={currentFrameNumber} doc={require('./assets/love.json')} />
        <br />
        <input type="range" defaultValue={currentFrameNumber} min={0} max={100} step={1} onChange={
          ({target:{valueAsNumber:currentFrameNumber}}) => this.setState({currentFrameNumber})
        } />
      </div>
    );
  }
}

document.write('<div id=KfSampleRoot></div>');

ReactDOM.render(
  <KfDemo />,
  document.getElementById('KfSampleRoot')
);
