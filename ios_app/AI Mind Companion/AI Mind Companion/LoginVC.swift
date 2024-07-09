//
//  LoginVC.swift
//  AI Mind Companion
//
//  Created by Josh Tai on 2/19/24.
//

import Foundation
import UIKit
import AVFoundation
import AVKit
import Firebase
import FirebaseCore
import FirebaseAuth
import GoogleSignIn
import GoogleSignInSwift

class LoginVC: UIViewController {

    @IBOutlet var userTextField    : UITextField!
    @IBOutlet var passTextField    : UITextField!
    @IBOutlet var enterButton      : UIButton!
    @IBOutlet var googleAuthButton : UIImageView!
    @IBOutlet var createAccButton  : UIButton!
    private   var audioPlayer      : AVAudioPlayer!

    public var accEmail : String = ""

    override func viewDidLoad() {
        super.viewDidLoad()
        
        setGradientBackground()
        
        // Configure Google Sign-In
        let config = GIDConfiguration(clientID: "338457398429-sk8i3938217607nik1ttorak5fdgmbmp.apps.googleusercontent.com")
        GIDSignIn.sharedInstance.configuration = config
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "goToNext" {
            if let tabBarController = segue.destination as? UITabBarController,
               let destinationVC = tabBarController.viewControllers?[0] as? AICompanionVC {
                destinationVC.accEmail = self.accEmail
            }
        }
    }

    @IBAction func emailTextFieldEdited(_ textField: UITextField) {
        let text = textField.text ?? ""
        accEmail = String(text)
    }

    // Login via email and password function
    @IBAction func enterButtonTapped (_ sender: UIButton) {
        print("Enter button tapped!")
        playSFX_ButtonClick()
        
        guard let email = userTextField.text else { return }
        guard let password = passTextField.text else { return }
        
        Auth.auth().signIn(withEmail: email, password: password) { firebaseResult, error in
            if error != nil {
                print("User login error.")
                self.playSFX_Error()
                
                let alert = UIAlertController(title: "Login error.",
                                              message: "Please re-enter valid login credentials.",
                                              preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                self.present(alert, animated: true, completion: nil)
                
            } else {
                print("Successfully logged in as \(self.accEmail)")
                self.accEmail = self.userTextField.text ?? ""
                
                let alert = UIAlertController(title: "Login Successful.",
                                              message: "Welcome to AI Mind Companion app.",
                                              preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { _ in self.nextScreen() }))
                self.present(alert, animated: true, completion: nil)
            }
        }
    }

    // Google Sign In function
    @IBAction func googleAuthTapped (_sender: UITapGestureRecognizer) {
        print("Google Auth button tapped!")
        playSFX_ButtonClick()
        
        // Google Sign In instance call
        GIDSignIn.sharedInstance.signIn(withPresenting: self) { [unowned self] result, error in
            guard error == nil
            else {
                print("Error signing in with Google: \(error?.localizedDescription ?? "error")")
                self.playSFX_Error()
                return
            }
            
            guard let user = result?.user, let idToken = user.idToken?.tokenString
            else {
                print("Google Sign-In did not return an idToken or authentication object.")
                return
            }
            
            let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: user.accessToken.tokenString)
            
            // Use the Google credential to sign in with Firebase
            Auth.auth().signIn(with: credential) { authResult, error in
                if let error = error {
                    print("Firebase sign in error: \(error)")
                    return
                }
            
            // User is now signed in to Firebase, proceed with your app flow
            print("User signed in to Firebase with Google")
            self.nextScreen()
            }
        }
    }

    @IBAction func createAccButtonTapped (_ sender: UIButton) {
        print("Create Account button tapped!")
        playSFX_ButtonClick()
    }

    @IBAction func dismissKeyboard(_ sender: UITapGestureRecognizer) {
        self.userTextField.resignFirstResponder()
        self.passTextField.resignFirstResponder()
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
