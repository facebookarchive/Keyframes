# Keyframes JS Source Code

## How to export animations from After Effects

See Keyframes After Effects Scripts/README.html

---

*The following are instructions for making changes to the JS code.  
**You don't need to do any of this stuff if you just want to run the script.***

## Setup

*Use [`yarn`](https://yarnpkg.com/) or [`npm`](https://www.npmjs.com/).*

Install dependencies.

    yarn install

Compile into Keyframes After Effects Scripts.

    yarn compile

File watcher, compile every time a file changes.

    yarn compile-watch

Run tests.

    yarn test

File watcher, run the tests every time a file changes.

    yarn test-watch

## AECompToKeyframesAnimation

Converts After Effects Comp JSON into Keyframes Animation descriptor JSON.

Usage example: *Convert an After Effects Comp JSON file into a Keyframes Animation descriptor JSON file.*

    var AECompToKeyframesAnimation = require('AECompToKeyframesAnimation');
    fs.writeFileSync(exportPath, JSON.stringify(AECompToKeyframesAnimation(fs.readFileSync(importPath))));

Usually this code is used by the Keyframes After Effects Scripts/FB Keyframes Exporter.jsx run by Adobe After Effects.
`/Keyframes After Effects Scripts/(lib)/keyframes/AECompToKeyframesAnimation.js` is a compiled version of this code. Compile using the instructions in the setup section above.
