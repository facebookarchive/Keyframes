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
var UI = exports;

UI.render = function(descriptor) {
  if (typeof descriptor.mount === 'function') {
    return descriptor.mount(descriptor);
  }
  if (!(descriptor.type in UI.mounters)) {
    return UI.mounters[DEFAULT_TYPE](descriptor);
  }
  return UI.mounters[descriptor.type](descriptor);
};

UI.mounters = {};

var DEFAULT_TYPE = 'group'

UI.mounters.group = addChild;

UI.mounters.palette =
UI.mounters.dialog =
UI.mounters.window = function(props){
  var properties = Object.assign({alignChildren: 'fill'}, props.properties);
  var win = new Window(props.type || 'window', props.title, props.bounds, properties);
  win.setProps = setProps;
  win.setProps(props);

  win.onShow = function(){ this.minimumSize = this.size; }
  // win.onResizing = win.onResize = function(){this.layout.resize();}
  return win;
};

var SetPropsBlacklist = {ref:1, render:1, type:1, properties:1, children:1, parent:1};

function setProps(props){
  var view = this;
  if (typeof props.render === 'function') {
    props = props.render.call(view, props);
  }
  var prop;
  for (var propName in props) {
    if (propName in SetPropsBlacklist) {
      // skip Object.prototype properties and blacklisted properties
      continue;
    }
    prop = props[propName];
    if (typeof prop == 'function') {
      prop = _wrapFn(prop);
    }
    view[propName] = prop;
  }
  if (Array.isArray(props.children)) {
    addChildren({
      parent: view,
      children: props.children,
    });
  }
  props.ref && props.ref(view);
  return view;
}

function addChildren(PROPS){
  return PROPS.children.map(function(childProps, index){
    if (!childProps) return;
    return addChild(Object.assign({parent:PROPS.parent}, childProps));
  });
}

function addChild(props){
  if (!(props && props.parent && props.parent.add)) {
    throw Error('Invalid "parent" prop');
  }
  var view = props.parent.add(props.type || DEFAULT_TYPE, props.bounds, props.text, props.properties);
  view.setProps = setProps;
  return view.setProps(props);
}

var _onError = function(error){
  console.error(error);
  alert(
    '(' + error.fileName +
    ':' + error.line +
    ':' + error.start +
    '-' + error.end +
    ') ' + error.message +
    '\n\n' +
    $.stack
  );
};

UI.setOnError = function(onError){
  if (typeof onError !== 'function') {
    throw Error('Invalid onError function');
  }
  _onError = onError;
}

function _wrapFn(fn){
  return function _wrapFnCaller(){
    try {
      var results = fn.apply(this, Array.prototype.slice.call(arguments));
    } catch(e) {
      if (_onError) {
        try {
          _onError(e);
        } catch(e1) {
          alert(e1.toString());
        }
      }
      throw e;
    }
  }
}
