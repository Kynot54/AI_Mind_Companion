//
//  CreateAccVC.swift
//  AI Mind Companion
//
//  Created by Josh Tai on 2/20/24.
//

import Foundation
import UIKit
import AVFoundation
import AVKit
import Firebase

class CreateAccVC: UIViewController {

    @IBOutlet var newUserTextField   : UITextField!
    @IBOutlet var newPassTextField   : UITextField!
    @IBOutlet var createNewAccButton : UIButton!
    @IBOutlet var backToLoginButton  : UIButton!
    private   var audioPlayer      : AVAudioPlayer!

    public var newAccEmail : String = ""

    override func viewDidLoad() {
        super.viewDidLoad()
        
        setGradientBackground()
    }

    @IBAction func createNewAccButtonTapped (_ sender: UIButton) {
        print("Create New Account button tapped!")
        playSFX_ButtonClick()
        
        guard let email = newUserTextField.text else { return }
        guard let password = newPassTextField.text else { return }
        
        Auth.auth().createUser(withEmail: email, password: password) { firebaseResult, error in
            if let e = error {
                print("Error creating new account: \(e.localizedDescription)")
                self.playSFX_Error()
                
                let alert = UIAlertController(title: "Error creating new account.",
                                              message: "Account already exists or email/password does not meet requirements.",
                                              preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                self.present(alert, animated: true, completion: nil)
                
            } else {
                print("Successfully created new account!")
                self.newAccEmail = self.newUserTextField.text ?? ""
                
                let alert = UIAlertController(title: "Successfully created new account.",
                                              message: "You can now login.",
                                              preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { _ in self.nextScreen() }))
                self.present(alert, animated: true, completion: nil)
            }
        }
    }

    @IBAction func backToLoginButtonTapped (_ sender: UIButton) {
        print("Back to Login button tapped!")
        playSFX_ButtonClick()
        nextScreen()
    }

    @IBAction func dismissKeyboard(_ sender: UITapGestureRecognizer) {
        self.newUserTextField.resignFirstResponder()
        self.newPassTextField.resignFirstResponder()
        print("Background tapped!")
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

    // Function to go to next segue screen
    func nextScreen() {
        self.performSegue(withIdentifier: "goToNext", sender: self)
    }

    func playSFX_ButtonClick() {
        if let audioPath = Bundle.main.path(forResource: "ButtonClick", ofType: "mp3") {
            do {
                let audioURL = URL(fileURLWithPath: audioPath)
                try self.audioPlayer = AVAudioPlayer(contentsOf: audioURL)
                self.audioPlayer.volume = 1
                self.audioPlayer.currentTime = 0
                self.audioPlayer.prepareToPlay()
                self.audioPlayer.play()
            } catch {
                print("Failed to play ButtonClick sound: \(error)")
            }
        }
    }

    func playSFX_Error() {
        if let audioPath = Bundle.main.path(forResource: "Error", ofType: "mp3") {
            do {
                let audioURL = URL(fileURLWithPath: audioPath)
                try self.audioPlayer = AVAudioPlayer(contentsOf: audioURL)
                self.audioPlayer.volume = 1
                self.audioPlayer.currentTime = 0
                self.audioPlayer.prepareToPlay()
                self.audioPlayer.play()
            } catch {
                print("Failed to play Error sound: \(error)")
            }
        }
    }

}
