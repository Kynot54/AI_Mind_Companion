//
//  HomeScreenVC.swift
//  AI Mind Companion
//
//  Created by Josh Tai on 2/16/24.
//

import Foundation
import UIKit
import AVFoundation
import AVKit

class HomeScreenVC: UIViewController {

    @IBOutlet var appLogoImage     : UIImageView!
    @IBOutlet var appTitle         : UILabel!
    @IBOutlet var appSubTitle      : UILabel!
    @IBOutlet var getStartedButton : UIButton!
    private   var audioPlayer      : AVAudioPlayer!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        setGradientBackground()
    }

    @IBAction func logoTapped (_sender: UITapGestureRecognizer) {
        print("AI Mind Companion logo tapped!")
        blinkLogo()
        playSFX_ShortCircuit()
    }

    @IBAction func getStartedButtonTapped (_ sender: UIButton) {
        print("Get Started button tapped!")
        playSFX_StartUp()
    }

    // Function to set gradient background
    func setGradientBackground() {
        let gradientLayer = CAGradientLayer()
        gradientLayer.colors = [
            UIColor(red: 98/255.0, green: 155/255.0, blue: 191/255.0, alpha: 1.0).cgColor,
            UIColor(red: 3/255.0, green: 185/255.0, blue: 174/255.0, alpha: 1.0).cgColor]
        gradientLayer.locations = [0.0, 1.0]
        gradientLayer.frame = view.bounds
        
        // Add the gradient layer to the view
        view.layer.insertSublayer(gradientLayer, at: 0)
    }

    // Function to activate blink effect in the logo
    private func blinkLogo() {
        UIView.animate(withDuration: 0.1, animations: {
            self.appLogoImage.alpha = 0
        }) { _ in
            UIView.animate(withDuration: 0.1) {
                self.appLogoImage.alpha = 1
            }
        }
    }

    func playSFX_ShortCircuit() {
        if let audioPath = Bundle.main.path(forResource: "ShortCircuit", ofType: "mp3") {
            do {
                let audioURL = URL(fileURLWithPath: audioPath)
                try self.audioPlayer = AVAudioPlayer(contentsOf: audioURL)
                self.audioPlayer.volume = 1
                self.audioPlayer.currentTime = 0
                self.audioPlayer.prepareToPlay()
                self.audioPlayer.play()
            } catch {
                print("Failed to play ShortCircuit sound: \(error)")
            }
        }
    }

    func playSFX_StartUp() {
        if let audioPath = Bundle.main.path(forResource: "StartUp", ofType: "mp3") {
            do {
                let audioURL = URL(fileURLWithPath: audioPath)
                try self.audioPlayer = AVAudioPlayer(contentsOf: audioURL)
                self.audioPlayer.volume = 0.5
                self.audioPlayer.currentTime = 0
                self.audioPlayer.prepareToPlay()
                self.audioPlayer.play()
            } catch {
                print("Failed to play StartUp sound: \(error)")
            }
        }
    }

}
