/**
 * Copyright (c) 2016-present, Facebook, Inc.
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
var UI = require('./(lib)/MudBrickLayer/ExtendScript/common/ui');

// Set this to true if you want to VERY VERY SLOWLY export a "keyframeTweens"
// array with the computed values of every property that has keyframes
VirtualTween.ENABLED = false;

function main(){
  AppWindow({
    title: 'FB Keyframes',
    onExport: exportActiveComp,
    log: function(message){
      exportActiveComp.logFile && exportActiveComp.logFile.write(new Date().toISOString() + '\t' + message);
    }
  });
}

function AppWindow(app){
  function Ref(id){ return function(view){ refs[id] = view; }; }
  var refs = {};

  function exportButtonOnClick(){
    refs.exportButton.onClick = function(){};
    refs.exportButton.enabled = false;
    refs.status.text = 'Exporting';
    var start = Date.now();

    app.onExport();

    refs.status.text = 'Done. ' + Math.round((Date.now() - start) / 100) / 10 + ' seconds';
    refs.exportButton.enabled = true;
    refs.exportButton.onClick = exportButtonOnClick;
  }

  UI.render(
    {ref:Ref('win'), type:'palette', title:app.title, alignChildren:'fill', children:[

      {orientation:'row', children:[
        {ref:Ref('exportButton'), type:'button', text:'Export', onClick:exportButtonOnClick},
        {ref:Ref('status'), type:'statictext', text:'Ready', characters:40},
      ]},

    ]}
  );

  refs.win.show();

  function writeToWindowLog(text) {
    refs.status.text = text;
    app.log && app.log(text);
  }
  console.configure({
    stdout:{write: writeToWindowLog},
    stderr:{write: writeToWindowLog},
  });
}

function exportActiveComp() {
  // Enable verbose logging in MudBrickLayer so we can show progress
  $.global.MudBrickLayer.debug = console.log.bind(console);

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

  if (Hooks.afterCreation.isSupported()) {
    Hooks.afterCreation.fire(filePathKf);
  } else {
    // TODO: Figure out how to preview on Windows
    File(filePathKf).parent.execute();
  }

  fs_writeFileSync(filePathRaw, activeItemJSON);
  console.log('Raw JSON Exported:');
  console.log(filePathRaw);

  $.global.MudBrickLayer.debug = undefined; // Disable verbose logging
  exportActiveComp.logFile.close();
}

var Hooks = {
  afterCreation: {
    isSupported: function(){
      return $.os.indexOf('Macintosh OS') === 0;
    },
    _file: File(__dirname + '/(lib)/hooks-macos/after-creation.sh'),
    fire: function(exportedFilePath){
      if (!this._file.exists) {
        console.error('After Creation Hook disabled. Enable by creating "' + this._file.fsName + '"');
        return;
      }
      var exportedFile = File(exportedFilePath);
      if (!exportedFile.exists) {
        console.error('After Creation Hook NOT called because the exported file does not exist: "' + exportedFile.fsName + '"');
        return;
      }
      var command = '"' + this._file.fsName + '" "' + exportedFile.fsName + '"';
      console.log('Calling After Creation Hook command: `' + command + '`');
      console.log(system.callSystem(command));
    }
  }
};

////////////////////////////////////////////////////////////////////////////////

main();

}());
