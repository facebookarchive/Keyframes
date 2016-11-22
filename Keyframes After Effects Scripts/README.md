# Keyframes After Effects Scripts

#### [How to enable scripts to read/write files/internet](https://helpx.adobe.com/after-effects/using/scripts.html)

> By default scripts are not allowed to write files or send or receive communication over a network. To allow scripts to write files and communicate over a network, choose Edit > Preferences > General (Windows) or After Effects > Preferences > General (Mac OS), and select the Allow Scripts To Write Files And Access Network option.

## FB Keyframes Exporter.jsx

Export the current After Effects comp to FB Keyframes Animation Descriptor JSON document.

> _Requires "**Allow Scripts To Write Files And Access Network**"_

### How to run

1.  Open an After Effects project
2.  Open the comp you want to export
3.  Menu item: **File / Scripts / Run Script File…**
4.  Choose `FB Keyframes Exporter.jsx`
    _The exporter palette window will open_
5.  Press Export

You'll see a bunch of progress messages.
Eventually the folder with the exported JSON will open.
You'll see three new files:

*   `Project.aep.comp-###-Comp Name.kf.json`  
    FB Keyframes compatible animation descriptor.

*   `Project.aep.comp-###-Comp Name.json`  
    Raw JSON description.

*   `Project.aep.comp-###-Comp Name.log`  
    Export progress messages and warnings with timestamps.
    Check this file for warnings about incompatible features.
    Also useful for diagnosing performance issues.

### How to install

1.  Quit After Effects
2.  Move the folder `Keyframes After Effects Scripts` into your AE Scripts folder
    _e.g. `/Applications/Adobe After Effects CC 2015/Scripts` on macOS_
3.  Launch After Effects
4.  Open an After Effects project
5.  Menu item: **File / Scripts / FB Keyframes Exporter.jsx**  
    _The exporter palette window will open_
6.  Press Export

You can keep the palette window open and press Export multiple times.
