//
//  SharedDataModel.swift
//  AI Mind Companion
//
//  Created by Josh Tai on 4/8/24.
//

import Foundation

class SharedDataModel {
    // Singleton instance
    static let shared = SharedDataModel()
    
    var accEmail: String?
    
    // Private initializer to ensure only one instance is created
    private init() {}
}
