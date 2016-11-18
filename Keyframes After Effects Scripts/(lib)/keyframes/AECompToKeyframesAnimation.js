/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 * 
 */
'use strict';var _slicedToArray=function(){function sliceIterator(arr,i){var _arr=[];var _n=true;var _d=false;var _e=undefined;try{for(var _i=arr[typeof Symbol==='function'?Symbol.iterator:'@@iterator'](),_s;!(_n=(_s=_i.next()).done);_n=true){_arr.push(_s.value);if(i&&_arr.length===i)break;}}catch(err){_d=true;_e=err;}finally{try{if(!_n&&_i["return"])_i["return"]();}finally{if(_d)throw _e;}}return _arr;}return function(arr,i){if(Array.isArray(arr)){return arr;}else if((typeof Symbol==='function'?Symbol.iterator:'@@iterator')in Object(arr)){return sliceIterator(arr,i);}else{throw new TypeError("Invalid attempt to destructure non-iterable instance");}};}();

















































function AECompToKeyframesAnimation(comp){
var kfDoc={
formatVersion:'1.0',
name:comp.name.replace(/[^a-z]/gi,'').toUpperCase(),
key:Number(comp.name.replace(/[^0-9]/g,'')),
frame_rate:comp.frameRate,
animation_frame_count:Math.round(comp.workAreaDuration*comp.frameRate),
canvas_size:[comp.width,comp.height],
features:[],
animation_groups:[]};


comp.layers.
filter(function(layer){return layer.enabled;}).
forEach(function(layer){
switch(layer.__type__){
case'AVLayer':
if(layer['source$__type__']==='FootageItem'&&layer['source$name'].endsWith('.png')){
// a image backed layer
var image=KFBitmapFeatureFromAVLayer(comp,layer,kfDoc);
if(image){
kfDoc.features.unshift(image);
}
}else{
var group=KfAnimationGroupFromAVLayer(comp,layer,kfDoc);
if(group){
kfDoc.animation_groups.unshift(group);
}
}
break;
case'ShapeLayer':
var shape=KfFeatureFromShapeLayer(comp,layer,kfDoc);
if(shape){
kfDoc.features.unshift(shape);
}
break;
default:
console.warn('Skipped unsupported layer type \''+layer.__type__+'\'');}

});

return kfDoc;
}

function commonFeatureFromLayer(
comp,
layer,
kfDoc)
{
var kfFeature={
name:layer.name};

if(layer.height!==comp.height||layer.width!==comp.width){
kfFeature.size=[layer.width,layer.height];
}
if(layer.parent$index){
kfFeature.animation_group=layer.parent$index;
}
if(layer.inPoint>0){
kfFeature.from_frame=Math.round(layer.inPoint*kfDoc.frame_rate);
}
if(Math.round(layer.outPoint*kfDoc.frame_rate)<kfDoc.animation_frame_count){
kfFeature.to_frame=Math.round(layer.outPoint*kfDoc.frame_rate);
}

// layer transforms
var transformGroup=getPropertyChild(layer,'ADBE Transform Group');
if(transformGroup){
kfFeature.feature_animations=parseTransformGroup(transformGroup,comp,layer,kfDoc);
}
return kfFeature;
}

function KFBitmapFeatureFromAVLayer(
comp,
layer,
kfDoc)
{
var kfFeature=commonFeatureFromLayer(comp,layer,kfDoc);
kfFeature.backed_image=layer['source$name'].substr(0,layer['source$name'].length-'.png'.length);
return kfFeature;
}

function KfFeatureFromShapeLayer(
comp,
layer,
kfDoc)
{
var kfFeature=commonFeatureFromLayer(comp,layer,kfDoc);

// layer shape
var rootVectorsGroup=getPropertyChild(layer,'ADBE Root Vectors Group');
if(!rootVectorsGroup){
throw'Root Vectors Group missing, corrupted input JSON';
}var _parseRootVectorsGrou=










parseRootVectorsGroup(rootVectorsGroup);var vectorShape=_parseRootVectorsGrou.vectorShape;var vectorFillColor=_parseRootVectorsGrou.vectorFillColor;var vectorStrokeColor=_parseRootVectorsGrou.vectorStrokeColor;var vectorStrokeWidth=_parseRootVectorsGrou.vectorStrokeWidth;var vectorStrokeLineCap=_parseRootVectorsGrou.vectorStrokeLineCap;var vectorPosition=_parseRootVectorsGrou.vectorPosition;var vectorScale=_parseRootVectorsGrou.vectorScale;var vectorRotation=_parseRootVectorsGrou.vectorRotation;var vectorOpacity=_parseRootVectorsGrou.vectorOpacity;

var shapeOffset=[0,0];

if(vectorPosition){
if(vectorPosition.keyframes){
warnIfUsingMissingFeature(true,'animation on vector position',vectorPosition,rootVectorsGroup,layer,comp);
}
shapeOffset=[vectorPosition.value[0],vectorPosition.value[1]];
}
if(vectorScale){
var key_values=keyValuesFor(comp,vectorScale,function(value){return[value[0],value[1]];});
warnIfUsingMissingFeature(key_values.filter(function(_ref){var _ref$data=_slicedToArray(_ref.data,2);var x=_ref$data[0];var y=_ref$data[1];return x!==100||y!==100;}).length>0,'Scale on vector',vectorScale,rootVectorsGroup,layer,comp);
}
if(vectorRotation){
var _key_values=keyValuesFor(comp,vectorRotation,function(value){return[value%360];});
warnIfUsingMissingFeature(_key_values.filter(function(_ref2){var _ref2$data=_slicedToArray(_ref2.data,1);var value=_ref2$data[0];return value!==0;}).length>0,'Rotation on vector',vectorRotation,rootVectorsGroup,layer,comp);
}

if(vectorStrokeLineCap){
switch(vectorStrokeLineCap.value){
case 1:
kfFeature.stroke_line_cap='butt';
break;
case 2:
kfFeature.stroke_line_cap='round';
break;
case 3:
kfFeature.stroke_line_cap='square';
break;}

}

if(vectorShape){
// set vector shapes
if(vectorShape.keyframes){
kfFeature.key_frames=vectorShape.keyframes.map(function(keyframe){
return{
start_frame:Math.round(keyframe.time*comp.frameRate),
data:parseShape(keyframe.value,shapeOffset)};

});
kfFeature.timing_curves=parseTimingFunctionsFromKeyframes(vectorShape.keyframes,parseShapeMorphTimingFunctions);
}else{
kfFeature.key_frames=[{
start_frame:0,
data:parseShape(vectorShape.value,shapeOffset)}];

}
}

if(vectorFillColor){
// set fill color
kfFeature.fill_color=getHexColorStringFromRGB(vectorFillColor.value);
}
if(vectorStrokeColor){
// set stroke color
kfFeature.stroke_color=getHexColorStringFromRGB(vectorStrokeColor.value);
}
if(vectorStrokeWidth){
// set stroke width
kfFeature.stroke_width=vectorStrokeWidth.value;
}

return kfFeature;
}

function componentToHex(
c)
{
var hex=Math.round(c*255).toString(16);
return hex.length===1?'0'+hex:hex;
}

function getHexColorStringFromRGB(_ref3)






{var _ref4=_slicedToArray(_ref3,4);var r=_ref4[0];var g=_ref4[1];var b=_ref4[2];var a=_ref4[3];
return"#"+componentToHex(a)+componentToHex(r)+componentToHex(g)+componentToHex(b);
}

function getPropertyChild(
layer,
childName)
{
var result=null;
if(layer){
layer.properties.forEach(function(property){
if(property.matchName===childName){
result=property;
}
});
}
return result;
}

function parseRootVectorsGroup(
rootGroup)











{
var vectorGroup=
getPropertyChild(rootGroup,'ADBE Vector Group');
var groupVectorsGroup=
getPropertyChild(vectorGroup,'ADBE Vectors Group');
var transformGroup=
getPropertyChild(vectorGroup,'ADBE Vector Transform Group');

var groupVectorShapeGroup=
getPropertyChild(groupVectorsGroup,'ADBE Vector Shape - Group');
var vectorShape=
getPropertyChild(groupVectorShapeGroup,'ADBE Vector Shape');

var vectorGraphicFill=
getPropertyChild(groupVectorsGroup,'ADBE Vector Graphic - Fill');
var vectorFillColor=
getPropertyChild(vectorGraphicFill,'ADBE Vector Fill Color');

var vectorGraphicStroke=
getPropertyChild(groupVectorsGroup,'ADBE Vector Graphic - Stroke');
var vectorStrokeColor=
getPropertyChild(vectorGraphicStroke,'ADBE Vector Stroke Color');
var vectorStrokeWidth=
getPropertyChild(vectorGraphicStroke,'ADBE Vector Stroke Width');
var vectorStrokeLineCap=
getPropertyChild(vectorGraphicStroke,'ADBE Vector Stroke Line Cap');

var vectorAnchor=
getPropertyChild(transformGroup,'ADBE Vector Anchor');
var vectorPosition=
getPropertyChild(transformGroup,'ADBE Vector Position');
var vectorScale=
getPropertyChild(transformGroup,'ADBE Vector Scale');
var vectorRotation=
getPropertyChild(transformGroup,'ADBE Vector Rotation');
var vectorOpacity=
getPropertyChild(transformGroup,'ADBE Vector Group Opacity');

return{
vectorShape:vectorShape,
vectorFillColor:vectorFillColor,
vectorStrokeColor:vectorStrokeColor,
vectorStrokeWidth:vectorStrokeWidth,
vectorStrokeLineCap:vectorStrokeLineCap,
vectorAnchor:vectorAnchor,
vectorPosition:vectorPosition,
vectorScale:vectorScale,
vectorRotation:vectorRotation,
vectorOpacity:vectorOpacity};

}






function hasTangent(_ref5)




{var _ref6=_slicedToArray(_ref5,2);var x=_ref6[0];var y=_ref6[1];
return!!x||!!y;
}

function getTangentCount(
pair1,
pair2)
{
var result=0;
if(hasTangent(pair1)){
++result;
}
if(hasTangent(pair2)){
++result;
}
return result;
}

/** parses a Shape to a SVG fashion string format */
function parseShape(
shape,
shapeOffset)
{var

inTangents=


shape.inTangents;var outTangents=shape.outTangents;var closed=shape.closed;

var vertices=
shape.vertices.map(function(vertex){return[vertex[0]+shapeOffset[0],vertex[1]+shapeOffset[1]];});
if(vertices.length===0){
return[];
}

var commands=[];
commands.push({
command:'M',
vertices:[vertices[0]]});

var currIndex=void 0;
var prevIndex=void 0;

for(var i=1;i<=vertices.length;++i){
if(i===vertices.length){
if(!closed){
continue;
}
prevIndex=vertices.length-1;
currIndex=0;
}else{
prevIndex=i-1;
currIndex=i;
}
var prevVertex=vertices[prevIndex];
var currVertex=vertices[currIndex];
var tangentCount=getTangentCount(outTangents[prevIndex],inTangents[currIndex]);

if(tangentCount===2){
commands.push({
command:'C',
vertices:[
[
prevVertex[0]+outTangents[prevIndex][0],
prevVertex[1]+outTangents[prevIndex][1]],

[
currVertex[0]+inTangents[currIndex][0],
currVertex[1]+inTangents[currIndex][1]],

[
currVertex[0],
currVertex[1]]]});



}else if(tangentCount===1){
if(hasTangent(outTangents[prevIndex])){
commands.push({
command:'Q',
vertices:[
[
prevVertex[0]+outTangents[prevIndex][0],
prevVertex[1]+outTangents[prevIndex][1]],

[
currVertex[0],
currVertex[1]]]});



}else if(hasTangent(inTangents[currIndex])){
commands.push({
command:'Q',
vertices:[
[
currVertex[0]+inTangents[currIndex][0],
currVertex[1]+inTangents[currIndex][1]],

[
currVertex[0],
currVertex[1]]]});



}
}else if(tangentCount===0){
commands.push({
command:'L',
vertices:[
currVertex]});


}
}

return commands.map(function(command){return(
command.command+command.vertices.map(function(v){return[v[0].toFixed(2),v[1].toFixed(2)];}).join(','));});

}


function parseTransformGroup(
transformGroup,
comp,
layer,
kfDoc)
{
var kfAnimGroupPropAnchorPointAnim=void 0;

// initialize default position at (0, 0)
var kfAnimGroupPropXPositionAnim=
{
property:'X_POSITION',
key_values:[
{
start_frame:0,
data:[0]}],


timing_curves:[]};

var kfAnimGroupPropYPositionAnim=
{
property:'Y_POSITION',
key_values:[
{
start_frame:0,
data:[0]}],


timing_curves:[]};

var kfAnimGroupPropRotationAnim=void 0;
var kfAnimGroupPropScaleAnim=void 0;
var kfAnimGroupPropOpacityAnim=void 0;

var animations=[];

transformGroup.properties.forEach(function(tfProp){
warnIfUsingMissingFeature(!!tfProp.expression,'expression',tfProp,transformGroup,layer,comp);
switch(tfProp.matchName){

case'ADBE Anchor Point':{
var timing_curves=parseTimingFunctionsFromKeyframes(tfProp.keyframes,parseTimingFunctions);
var key_values=keyValuesFor(comp,tfProp,function(value){return[value[0],value[1]];});
kfAnimGroupPropAnchorPointAnim={property:'ANCHOR_POINT',key_values:key_values,timing_curves:timing_curves};
}break;

case'ADBE Position':{
var _timing_curves=parseTimingFunctionsFromKeyframes(tfProp.keyframes,parseTimingFunctions);
var x_key_values=keyValuesFor(comp,tfProp,function(value){return[value[0]];});
var y_key_values=keyValuesFor(comp,tfProp,function(value){return[value[1]];});
if(x_key_values.filter(function(_ref7){var _ref7$data=_slicedToArray(_ref7.data,1);var value=_ref7$data[0];return value!==0;}).length>0){
kfAnimGroupPropXPositionAnim={property:'X_POSITION',key_values:x_key_values,timing_curves:_timing_curves};
}
if(y_key_values.filter(function(_ref8){var _ref8$data=_slicedToArray(_ref8.data,1);var value=_ref8$data[0];return value!==0;}).length>0){
kfAnimGroupPropYPositionAnim={property:'Y_POSITION',key_values:y_key_values,timing_curves:_timing_curves};
}
}break;

case'ADBE Position_0':{
var _timing_curves2=parseTimingFunctionsFromKeyframes(tfProp.keyframes,parseTimingFunctions);
var _key_values2=keyValuesFor(comp,tfProp,function(value){return[value];});
if(_key_values2.filter(function(_ref9){var _ref9$data=_slicedToArray(_ref9.data,1);var value=_ref9$data[0];return value!==0;}).length>0){
kfAnimGroupPropXPositionAnim={property:'X_POSITION',key_values:_key_values2,timing_curves:_timing_curves2};
}
}break;

case'ADBE Position_1':{
var _timing_curves3=parseTimingFunctionsFromKeyframes(tfProp.keyframes,parseTimingFunctions);
var _key_values3=keyValuesFor(comp,tfProp,function(value){return[value];});
if(_key_values3.filter(function(_ref10){var _ref10$data=_slicedToArray(_ref10.data,1);var value=_ref10$data[0];return value!==0;}).length>0){
kfAnimGroupPropYPositionAnim={property:'Y_POSITION',key_values:_key_values3,timing_curves:_timing_curves3};
}
}break;

case'ADBE Scale':{
var _timing_curves4=parseTimingFunctionsFromKeyframes(tfProp.keyframes,parseTimingFunctions);
var _key_values4=keyValuesFor(comp,tfProp,function(value){return[value[0],value[1]];});
if(_key_values4.filter(function(_ref11){var _ref11$data=_slicedToArray(_ref11.data,2);var x=_ref11$data[0];var y=_ref11$data[1];return x!==100||y!==100;}).length>0){
kfAnimGroupPropScaleAnim={property:'SCALE',key_values:_key_values4,timing_curves:_timing_curves4};
}
}break;

case'ADBE Opacity':{
if(layer.matchName==='ADBE Vector Layer'){
var _timing_curves5=parseTimingFunctionsFromKeyframes(tfProp.keyframes,parseTimingFunctions);
var _key_values5=keyValuesFor(comp,tfProp,function(value){return[value];});
if(_key_values5.filter(function(_ref12){var _ref12$data=_slicedToArray(_ref12.data,1);var value=_ref12$data[0];return value!==100;}).length>0){
kfAnimGroupPropOpacityAnim={property:'OPACITY',key_values:_key_values5,timing_curves:_timing_curves5};
}
}
}break;

case'ADBE Rotate Z':{
var _timing_curves6=parseTimingFunctionsFromKeyframes(tfProp.keyframes,parseTimingFunctions);
var _key_values6=keyValuesFor(comp,tfProp,function(value){return[value];});
if(_key_values6.filter(function(_ref13){var _ref13$data=_slicedToArray(_ref13.data,1);var value=_ref13$data[0];return value%360!==0;}).length>0){
kfAnimGroupPropRotationAnim={property:'ROTATION',key_values:_key_values6,timing_curves:_timing_curves6};
}
}break;

default:
warnIfUsingMissingFeature(tfProp.isModified,tfProp.__type__,tfProp,transformGroup,layer,comp);}

});

kfAnimGroupPropAnchorPointAnim&&animations.push(kfAnimGroupPropAnchorPointAnim);
kfAnimGroupPropXPositionAnim&&animations.push(kfAnimGroupPropXPositionAnim);
kfAnimGroupPropYPositionAnim&&animations.push(kfAnimGroupPropYPositionAnim);
kfAnimGroupPropScaleAnim&&animations.push(kfAnimGroupPropScaleAnim);
kfAnimGroupPropRotationAnim&&animations.push(kfAnimGroupPropRotationAnim);
kfAnimGroupPropOpacityAnim&&animations.push(kfAnimGroupPropOpacityAnim);
return animations;
}

function KfAnimationGroupFromAVLayer(
comp,
layer,
kfDoc)
{
/*
    A KfAnimationGroup is a Null parent of a Shape or a Null
  */
var kfAnimGroup={
group_id:layer.index,
group_name:layer.name,
animations:[]};

if(layer.parent$index){
kfAnimGroup.parent_group=layer.parent$index;
}

layer.properties.forEach(function(layerProp){
switch(layerProp.matchName){
case'ADBE Transform Group':
kfAnimGroup.animations=parseTransformGroup(layerProp,comp,layer,kfDoc);
break;
default:
if(layerProp.__type__==='PropertyGroup'){
warnIfUsingMissingFeature(layerProp.properties.length>0,layerProp.__type__,layerProp,layer,comp);
}else{
warnIfUsingMissingFeature(layerProp.isModified,layerProp.__type__,layerProp,layer,comp);
}}

});

return kfAnimGroup;
}

var parseTimingFunctionsFromKeyframes=function parseTimingFunctionsFromKeyframes(keyframes,mapping){return(
keyframes==null?[]:
keyframes.map(function(keyframe,index){return(
mapping(keyframe,keyframes&&keyframes[index+1]));}).
filter(Boolean));};


var parseShapeMorphTimingFunctions=function parseShapeMorphTimingFunctions(keyframeA,keyframeB){return(
keyframeA&&keyframeB&&
[
[0+keyframeA.outTemporalEase[0].influence/200,0],
[1-keyframeB.inTemporalEase[0].influence/200,1]]);};



var parseTimingFunctions=function parseTimingFunctions(keyframeA,keyframeB){
if(!keyframeA||!keyframeB){
return null;
}

if(keyframeA.outInterpolationType==='LINEAR'&&keyframeB.inInterpolationType==='LINEAR'){
return[
[1.0/3.0,1.0/3.0],
[2.0/3.0,2.0/3.0]];

}
warnIfUsingMissingFeature(keyframeA.outInterpolationType!=='BEZIER',
'Unsupported interpolation method \''+keyframeA.outInterpolationType+'\'');
warnIfUsingMissingFeature(keyframeB.inInterpolationType!=='BEZIER',
'Unsupported interpolation method \''+keyframeB.inInterpolationType+'\'');

var keyframeAOut=keyframeA.outTemporalEase[0];
var keyframeBIn=keyframeB.inTemporalEase[0];
var duration=keyframeB.time-keyframeA.time;
var distance=0;
if(Array.isArray(keyframeA.value)){
distance=Math.sqrt((keyframeA.value[0]-keyframeB.value[0])*(keyframeA.value[0]-keyframeB.value[0])+
(keyframeA.value[1]-keyframeB.value[1])*(keyframeA.value[1]-keyframeB.value[1]));
}else{
distance=Math.abs(keyframeB.value-keyframeA.value);
}

var cp1Value=0.0;
var cp2Value=1.0;

if(distance!==0){
cp1Value+=duration/distance*Math.abs(keyframeAOut.speed)*(keyframeAOut.influence/100);
cp2Value-=duration/distance*Math.abs(keyframeBIn.speed)*(keyframeBIn.influence/100);
}
var cp1=[
0.0+keyframeAOut.influence/100,
cp1Value];

var cp2=[
1.0-keyframeBIn.influence/100,
cp2Value];


return[cp1,cp2];
};

function keyValuesFor(
comp,
tfProp,
convert)
{
var keyValues=[];
tfProp.keyframes&&tfProp.keyframes.forEach(function(_ref14){var time=_ref14.time;var value=_ref14.value;
var data=convert(value);
keyValues.push({start_frame:Math.round(time*comp.frameRate),data:data});
});
if(keyValues.length===0){
var data=convert(tfProp.value);
keyValues.push({start_frame:0,data:data});
}
return keyValues;
}

function warnIfUsingMissingFeature(shouldWarn,feature){
if(!shouldWarn){
return;
}for(var _len=arguments.length,objects=Array(_len>2?_len-2:0),_key=2;_key<_len;_key++){objects[_key-2]=arguments[_key];}
var keyPath=objects.map(function(_ref15){var name=_ref15.name;return name;}).reverse().concat(feature);
var comp=objects[objects.length-1];
keyPath.unshift(comp&&comp.parentFolder$name);
console.warn('UNSUPPORTED: %s',keyPath.join(' → '));
}

module.exports=AECompToKeyframesAnimation;