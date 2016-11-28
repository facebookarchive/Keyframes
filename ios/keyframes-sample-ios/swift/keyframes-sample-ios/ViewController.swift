/* Copyright 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-sample file in the root directory of this source tree.
 */

import UIKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let sampleVector : KFVector!
        
        do {
            sampleVector = try self.loadSampleVectorFromDisk()
        } catch {
            print("Vector file could not be loaded, aborting")
            return
        }
        
        let sampleVectorLayer : KFVectorLayer = KFVectorLayer()
        
        let shortSide = min(self.view.bounds.width, self.view.bounds.height)
        let longSide = max(self.view.bounds.width, self.view.bounds.height)
        
        sampleVectorLayer.frame = CGRect(x: shortSide / 4, y: longSide / 2 - shortSide / 4, width: shortSide / 2, height: shortSide / 2)
        sampleVectorLayer.faceModel = sampleVector!
        
        self.view.layer.addSublayer(sampleVectorLayer)
        sampleVectorLayer.startAnimation()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func loadSampleVectorFromDisk() throws -> KFVector {
        let filePath : String = Bundle(for: type(of: self)).path(forResource: "sample_logo", ofType: "json")!
        let data : Data = try String(contentsOfFile: filePath).data(using: .utf8)!
        let sampleVectorDictionary : Dictionary = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as! [String:Any]
        
        return KFVectorFromDictionary(sampleVectorDictionary)
    }
}
