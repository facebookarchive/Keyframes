/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
'use strict';

var AECompToKeyframesAnimation=require('./AECompToKeyframesAnimation');

var args=process.argv.slice(2);
var fs=require('fs');
var parsedData=JSON.parse(fs.readFileSync(args[0],'utf-8'));

var kfDoc=AECompToKeyframesAnimation(parsedData);
console.log(JSON.stringify(kfDoc,null,2));

var requiredAsset=new Set();
kfDoc.features.forEach(function(feature){
if(feature.backed_image){
requiredAsset.add(feature.backed_image);
}
});

if(requiredAsset.size>0){
console.error('');
console.error('Please provide following image assets in your code: ');
console.error('');
console.error('Code template on iOS:');
console.error('$KfVectorLayer$.imageAssets = @{');
for(var _iterator=requiredAsset,_isArray=Array.isArray(_iterator),_i=0,_iterator=_isArray?_iterator:_iterator[typeof Symbol==='function'?Symbol.iterator:'@@iterator']();;){var _ref;if(_isArray){if(_i>=_iterator.length)break;_ref=_iterator[_i++];}else{_i=_iterator.next();if(_i.done)break;_ref=_i.value;}var imageName=_ref;
console.error('  @"'+imageName+'": $UIImage$,');
}
console.error('};');
}