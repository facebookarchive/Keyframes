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
var KeyframesPreviewer = require('./(lib)/KeyframesPreviewer');

// Set this to true if you want to VERY VERY SLOWLY export a "keyframeTweens"
// array with the computed values of every property that has keyframes
VirtualTween.ENABLED = false;

function main(){
  if ($.global.KeyframesPreviewerPanel) {
    try{$.global.KeyframesPreviewerPanel.close();}catch(e){}
  }

  var previousTime = Date.now();
  AppWindow({
    log: function(message){
      var  timeStamp = new Date();
      var duration = timeStamp.getTime() - previousTime;
      if (exportActiveComp.logFile) {
        try {
          exportActiveComp.logFile.write(timeStamp.toISOString() + '\t(' + duration + ')\t' + message);
        } catch(e){
          exportActiveComp.logFile.write(timeStamp.toString() + '\t(' + duration + ')\t' + message);
        }
      } else {
        $.level > 0 && $.writeln(message);
      }
      previousTime = timeStamp.getTime();
    }
  });
}

function AppWindow(appProps){
  var refs = {};
  function Ref(id){ return function(view){ refs[id] = view; }; }
  function setStatusText(message) {
    if (refs.status) {
      refs.status.text = message.toString();
    }
  }

  UI.render(
    {ref:Ref('win'), type:'palette', title:'Keyframes Exporter + Previewer', alignChildren:'fill', children:[

      {orientation:'row', children:[
        {ref:Ref('exportButton'), type:'button', text:'Export', onClick:function(){
          setStatusText('Exporting');
          var start = Date.now();
          var results = exportActiveComp({shouldPreview:true});
          setStatusText('Done. ' + Math.round((Date.now() - start) / 100) / 10 + ' seconds');
        }},

        {ref:Ref('status'), type:'edittext', properties:{readonly:true}, text:'Ready', characters:40},

        {type:'button', text:'Help', onClick:function(){
          confirm('Open the README.html file for more info...') && File(__dirname).execute();
        }},
      ]},

      {orientation:'row', children:[
        {type:'button', text:'Resend', onClick:function(){
          var kfFile = File(File(getCompRawJSONFile().fsName).fsName.replace('.json', '.kf.json'));
          setStatusText(kfFile.toString());
          var results = KeyframesPreviewer.previewJSONAtPath(kfFile.fsName);
          console.log(results);
        }},

        {type:'button', text:'Open Export Logs', onClick:function(){
          var logFile = File(File(getCompRawJSONFile().fsName).fsName.replace('.json', '.log'));
          setStatusText(logFile.toString());
          logFile.execute();
        }},

        {type:'button', text:'Open Folder', onClick:function(){ getCompRawJSONFile().parent.execute(); }},

        {ref:Ref('installButton'), type:'button', text:'Install Android App', onClick:function(){
          setStatusText('Installing...');
          var results = KeyframesPreviewer.install();
          alert(results);
          setStatusText('Installed');
        }},
      ]},

    ]}
  );

  $.global.KeyframesPreviewerPanel = refs.win;

  refs.win.show();

  console.configure({
    stdout:{write: function(text){
      appProps.log && appProps.log(text);
      setStatusText(text);
    }},
    stderr:{write: function(text){
      appProps.log && appProps.log(text);
    }},
  });
}

function getCompRawJSONFile(){
  var activeComp = app.project && app.project.activeItem;
  if (!activeComp) {
    alert('Failed to get the activeItem. No comp selected? Select a comp and try again');
  }
  var activeItemObject = activeComp.toJSON();
  if (!activeItemObject) {
    alert('Failed to convert the selected item');
  }
  var cleanName = activeItemObject.name.replace(/[^a-z0-9 _-]/ig, '');
  var projectFile = app.project.file;
  if (projectFile == null) {
    throw Error('Project file does not exist. Save your ptoject and try again.');
  }
  var filePathRaw = projectFile.fsName + '.comp-' + activeItemObject.id + '-' + cleanName + '.json';
  return File(filePathRaw);
}

function exportActiveComp(props) {
  // Enable verbose logging in MudBrickLayer so we can show progress
  $.global.MudBrickLayer.debug = console.log.bind(console);
  // $.global.MudBrickLayer.debug = null; // DEBUG

  var rawExportFile = getCompRawJSONFile();
  var filePathRaw = rawExportFile.fsName;
  var filePathKf = filePathRaw.replace('.json', '.kf.json');
  var filePathLog = filePathRaw.replace('.json', '.log');

  exportActiveComp.logFile = File(filePathLog);
  exportActiveComp.logFile.open('w');

  console.log('Exporting "' + app.project.activeItem.name + '" from "' + app.project.file.fsName + '"');

  console.time('toJSON export');
  var activeItemJSONObject = app.project.activeItem.toJSON();
  console.timeEnd('toJSON export');

  console.log('Encoding...');
  console.time('stringify export');
  var activeItemJSON = JSON.stringify(activeItemJSONObject, null, 2);
  console.timeEnd('stringify export');

  // Make sure we're dealing with basic JS objects
  // instead of fancy ExtendScript class instances
  console.log('Cloning...');
  console.time('clone export');
  var activeItemObjectClone = eval('(' + activeItemJSON + ')');
  console.timeEnd('clone export');

  console.log('Exporting...');
  console.time('AECompToKeyframesAnimation');
  var keyframesDocument = AECompToKeyframesAnimation(activeItemObjectClone);
  console.timeEnd('AECompToKeyframesAnimation');

  // Quick fix to work around expected int value
  keyframesDocument.frame_rate = Math.round(keyframesDocument.frame_rate);

  console.log('Saving...');
  console.time('saving');
  fs_writeFileSync(filePathKf, JSON.stringify(keyframesDocument, null, 2));
  console.timeEnd('saving');
  console.log('FB Keyframes JSON Exported:');
  console.log(filePathKf);

  var results;
  if (props && props.shouldPreview) {
    results = KeyframesPreviewer.previewJSONAtPath(filePathKf);
    console.log(results);
  } else {
    console.warn('KeyframesPreviewer is disabled');
  }

  $.global.MudBrickLayer.debug = undefined; // Disable verbose logging
  exportActiveComp.logFile.close();

  return results;
}

////////////////////////////////////////////////////////////////////////////////

try {
  main();
} catch(e){
  alert(e.toString());
  throw e;
}

}());
