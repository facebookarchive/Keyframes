---
docid: getting-started
title: Getting Started
layout: docs
permalink: /docs/getting-started
---

Keyframes is a combination of (1) an exporting script that extracts image animation data from an After Effects composition and (2) a corresponding rendering library for Android and iOS. Keyframes can be used to export and render high quality, vector based animations with complex shape and path curves, all with minimal file footprint.

For sample files, check [here](sample-files).

## After Effects

#### Developing animations

To keep Keyframes simple, we support a limited number of features in After Effects.  For a detailed list of guidelines for developing animations to use with the Keyframes library, please refer to the [Keyframes After Effects Guidelines](ae/guidelines).

#### Exporting animations

We support two different methods for exporting compositions from After Effects: a command line interface which requires Node.js, and an After Effects plugin.  More detailed information is in the [exporting guide](ae/exporting).

## iOS Installation

Keyframes is available on CocoaPods. Add the following to your Podfile:

```
target 'MyApp' do
  pod "Keyframes"
end
```

Quit Xcode compeletly before running

```
pod install
```

in the project directory in terminal.

## Android Installation

Download [the latest JARs](https://github.com/facebookincubator/keyframes/releases/latest) or grab via Gradle:

```groovy
compile 'com.facebook.keyframes:keyframes:1.0'
```
or Maven:

```xml
<dependency>
  <groupId>com.facebook.keyframes</groupId>
  <artifactId>keyframes</artifactId>
  <version>1.0</version>
</dependency>
```
