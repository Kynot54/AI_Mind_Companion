//
//  LogoutVC.swift
//  AI Mind Companion
//
//  Created by Josh Tai on 2/22/24.
//

import Foundation
import UIKit
import AVFoundation
import AVKit
import CoreData

class LogoutVC: UIViewController {

    @IBOutlet var titleLabel   : UILabel!
    @IBOutlet var logoutButton : UIButton!
    public    var accEmail     : String = ""
    private   var audioPlayer  : AVAudioPlayer!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        accEmail = SharedDataModel.shared.accEmail ?? ""
        print("accEmail: " + accEmail)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        playSFX_Fuuu()
    }

    @IBAction func logoutButtonTapped(_ sender: UIButton) {
        playSFX_R2D2_Robot()
    }

    func playSFX_R2D2_Robot() {
        if let audioPath = Bundle.main.path(forResource: "R2D2_Robot", ofType: "mp3") {
            do {
                let audioURL = URL(fileURLWithPath: audioPath)
                try self.audioPlayer = AVAudioPlayer(contentsOf: audioURL)
                self.audioPlayer.volume = 1
                self.audioPlayer.currentTime = 0
                self.audioPlayer.prepareToPlay()
                self.audioPlayer.play()
            } catch {
                print("Failed to play R2D2_Robot sound.")
            }
        }
    }

    func playSFX_Fuuu() {
        if let audioPath = Bundle.main.path(forResource: "Fuuu", ofType: "mp3") {
            do {
                let audioURL = URL(fileURLWithPath: audioPath)
                try self.audioPlayer = AVAudioPlayer(contentsOf: audioURL)
                self.audioPlayer.volume = 1
                self.audioPlayer.currentTime = 0
                self.audioPlayer.prepareToPlay()
                self.audioPlayer.play()
            } catch {
                print("Failed to play Fuuu sound.")
            }
        }
    }



}
