//
//  FileManager.swift
//  SwiftyJodel
//
//  Created by Lothar Mödl on 25.04.17.
//
//


import Foundation
import HeliumLogger
import LoggerAPI

public typealias Command = String

public struct Shell {
    
    public static func execute(_ command: Command, withPassword password: String? = nil) throws {
        let pipe = Pipe()
        
        
        let process = Process()

        process.standardOutput = pipe
        process.launchPath = "/bin/sh"
        if password == nil {
            process.arguments = ["-c", command]
        } else {
            process.arguments = ["-c", "echo \(password!) | sudo -S \(command)"]
        }
        process.launch()
        
        if let output = String(data: pipe.fileHandleForReading.readDataToEndOfFile(), encoding: String.Encoding.utf8) {
            NSLog(output)
        }
        
        process.waitUntilExit()
        Log.verbose("Process Exited")
        //process.terminate()
    }
    
}


