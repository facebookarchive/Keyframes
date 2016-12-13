/* Copyright 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-sample file in the root directory of this source tree.
 */

import Cocoa
import Keyframes

class ViewController: NSViewController {

    private var sampleVectorLayer: KFVectorLayer!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.wantsLayer = true
        view.layer = CALayer()
        view.layer!.backgroundColor = NSColor.white.cgColor
        
        let sampleVector : KFVector!
        
        do {
            sampleVector = try self.loadSampleVectorFromDisk()
        } catch {
            print("Vector file could not be loaded, aborting")
            return
        }
        
        sampleVectorLayer = KFVectorLayer()
        sampleVectorLayer.frame = CGRect(x: view.bounds.width / 2 - 200, y: view.bounds.height / 2 - 200, width: 400, height: 400)
        sampleVectorLayer.faceModel = sampleVector!
        
        self.view.layer!.addSublayer(sampleVectorLayer)
        sampleVectorLayer.startAnimation()
        
        return
    }
    
    override func viewDidLayout() {
        super.viewDidLayout()
        
        // resize vector layer (without animation) to be centered within the view
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        CATransaction.setAnimationDuration(0)
        sampleVectorLayer.frame = CGRect(x: view.bounds.width / 2 - 200, y: view.bounds.height / 2 - 200, width: 400, height: 400)
        CATransaction.commit()
    }
    
    func loadSampleVectorFromDisk() throws -> KFVector {
        let filePath : String = Bundle(for: type(of: self)).path(forResource: "sample_logo", ofType: "json")!
        let data : Data = try String(contentsOfFile: filePath).data(using: .utf8)!
        let sampleVectorDictionary : Dictionary = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [String:Any]
        
        return KFVectorFromDictionary(sampleVectorDictionary)
    }

}

