/**
 * Copyright (c) 2013-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 * Run this script in Adobe After Effects
 */
var __dirname = File($.fileName).parent.fsName;

$.level = 0; // disable debugging

// JS alternative to #include "./(lib)/MudBrickLayer/index.jsxinc"
$.evalFile(File(__dirname + '/(lib)/MudBrickLayer/ExtendScript/AE/index-ae.jsxinc'));
require.wasIncludedFrom($.fileName); // make relative requires work

;(function(){

var AECompToKeyframesAnimation = require('./(lib)/keyframes/AECompToKeyframesAnimation');

// Set this to true if you want to VERY VERY SLOWLY export a "keyframeTweens"
// array with the computed values of every property that has keyframes
VirtualTween.ENABLED = false;

$.global.MudBrickLayer.debug = console.log.bind(console);

function main(){
  initWindow({
    title: 'FB Keyframes',
    onExport: exportActiveComp,
  });
}

function initWindow(config){
  var win = new Window('palette', config.title, undefined, {alignChildren:'fill'});
  win.orientation = 'row';
  win.onShow = function(){ this.minimumSize = this.size; }
  // win.onResizing = win.onResize = function(){this.layout.resize();}

  var exportButton = win.add('button', undefined, 'Export', {});
  exportButton.onClick = exportButtonClick;
  function exportButtonClick(){
    exportButton.onClick = function(){};
    exportButton.enabled = false;
    textView.text = 'Exporting';
    var start = Date.now();

    config.onExport();

    textView.text = 'Done. ' + Math.round((Date.now() - start) / 100) / 10 + ' seconds';
    exportButton.enabled = true;
    exportButton.onClick = exportButtonClick;
  };

  textView = win.add('statictext', undefined, 'Ready to export', {});
  textView.characters = 40;
  win.show();

  function writeToWindowLog(text) {
    textView.text = text;
    exportActiveComp.logFile && exportActiveComp.logFile.write(new Date().toISOString() + '\t' + text);
  }
  console.configure({
    stdout:{write: writeToWindowLog},
    stderr:{write: writeToWindowLog},
  });
}

function exportActiveComp() {
  var activeComp = app.project && app.project.activeItem;
  if (!activeComp) {
    alert('Failed to get the activeItem. No comp selected? Select a comp and try again');
  }
  var activeItemObject = activeComp.toJSON();
  if (!activeItemObject) {
    alert('Failed to convert the selected item');
  }
  var cleanName = activeItemObject.name.replace(/[^a-z0-9 _-]/ig, '');
  var filePathRaw = app.project.file.fsName + '.comp-' + activeItemObject.id + '-' + cleanName + '.json';
  var filePathKf = filePathRaw.replace('.json', '.kf.json');
  var filePathLog = filePathRaw.replace('.json', '.log');

  exportActiveComp.logFile = File(filePathLog);
  exportActiveComp.logFile.open('w');
  
  console.log('Exporting "' + cleanName + '" from "' + app.project.file.fsName + '"');

  console.log('Encoding...');
  var activeItemJSON = JSON.stringify(activeItemObject, null, 2);

  // Make sure we're dealing with basic JS objects
  // instead of fancy ExtendScript class instances
  console.log('Cloning...');
  var activeItemObjectClone = eval('(' + activeItemJSON + ')');

  console.log('Exporting...');
  var keyframesDocument = AECompToKeyframesAnimation(activeItemObjectClone);

  console.log('Saving...');
  fs_writeFileSync(filePathKf, JSON.stringify(keyframesDocument, null, 2));
  console.log('FB Keyframes JSON Exported:');
  console.log(filePathKf);

  File(filePathKf).parent.execute();

  fs_writeFileSync(filePathRaw, activeItemJSON);
  console.log('Raw JSON Exported:');
  console.log(filePathRaw);

  exportActiveComp.logFile.close();
}

main();

}());
