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

class KeyframesSample extends React.Component {
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
  constructor(props, ...args) {
    super(props, ...args);
    this.handleRAF = this.handleRAF.bind(this);
    console.log(props)
  }
  componentDidMount() {
    this.tickRAF();
  }
  tickRAF() {
    this._rafTimer = requestAnimationFrame(this.handleRAF);
  }
  componentWillUnmount() {
    cancelAnimationFrame(this._rafTimer);
  }
  handleRAF() {
    const now = Date.now();
    if (!(now < this.nextFrameStartTime)) {
      let currentFrame = (this.state || this.props).currentFrame || 0;
      currentFrame ++;
      const {frame_rate, animation_frame_count} = this.props.doc;
      const frameTime = 1000 / frame_rate
      this.nextFrameStartTime = now + frameTime;
      
      if (currentFrame > animation_frame_count) {
        if (!this.props.loop) {
          return;
        }
        currentFrame = 0;
      }
      this.setState({currentFrame});
    }
    this.tickRAF();
  }
  render() {
    // console.log(this.props.doc);
    const {features} = this.props.doc;
    const {currentFrame} = this.state || this.props;
    console.log({currentFrame})
    return (
      <div>
        <p>Yo?!</p>
        <Surface width={177} height={177}>
          {features.map(({fill_color, stroke_color, stroke_width, key_frames}, index) => {
            const shapeData = getShapeDataForCurrentFrame(key_frames, currentFrame);
            return shapeData && (
              <Shape key={index}
                fill={hexColorSwapAlphaPosition(fill_color)}
                stroke={hexColorSwapAlphaPosition(stroke_color)}
                strokeWidth={stroke_width}
                d={shapeData.join(' ')}
              />
            );
          })}
        </Surface>
      </div>
    );
  }
}
KeyframesSample.defaultProps = {
  currentFrame: 0,
  loop: false,
};

function hexColorSwapAlphaPosition(color) {
  if (!color) {
    return;
  }
  return '#' + color.substr(3,6) + color.substr(1,2);
}


function getShapeDataForCurrentFrame(key_frames, targetFrame): ?Array<string> {
  let kFrame;
  if (key_frames.length > 0) {
    let _kFrame, targetFrameBestMatch = -1;
    for (let index = key_frames.length; --index >= 0;){
      _kFrame = key_frames[index];
      const {start_frame} = _kFrame;
      // There can't be more than one perfect match
      if (start_frame === targetFrame) {
        kFrame = _kFrame;
        break;
      }
      // Skip any that happen later than now
      if (start_frame > targetFrame) {
        continue;
      }
      // Keep any that are closer to the target
      if (start_frame >= targetFrameBestMatch) {
        targetFrameBestMatch = start_frame;
        kFrame = _kFrame;
      }
    }
  }
  return kFrame && kFrame.data || null;
}


// function filterStartFrameToThis(({start_frame}), index, key_frames): boolean {
//   const targetFrame = this;
//   const previousFrame = key_frames[index - 1];
//   const previousStartFrame = previousFrame && previousFrame.start_frame || NaN;
//   const nextFrame = key_frames[index + 1];
//   const nextStartFrame = nextFrame && nextFrame.start_frame || NaN;
//
//   if (nextStartFrame <= targetFrame) {
//     return false;
//   }
//   return true;
// }


document.write('<div id=KeyframesSampleRoot></div>');

ReactDOM.render(
  <div>
    <KeyframesSample doc={require('./assets/sample_anger_temp.json')} />
    <KeyframesSample doc={require('./assets/sample_haha.json')} />
    <KeyframesSample doc={require('./assets/sample_like.json')} />
  </div>,
  document.getElementById('KeyframesSampleRoot')
);
