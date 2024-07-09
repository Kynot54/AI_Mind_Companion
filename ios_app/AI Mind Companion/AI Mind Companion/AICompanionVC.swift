//
//  AICompanionVC.swift
//  AI Mind Companion
//
//  Created by Josh Tai on 2/22/24.
//

import Foundation
import UIKit
import AVFoundation
import AVKit
import CoreData

class AICompanionVC: UIViewController, AVAudioRecorderDelegate, AVAudioPlayerDelegate {

    private var micImage           : UIImageView!
    private var micRecStartIcon    : UIImageView!
    private var micRecStopIcon     : UIImageView!
    private var verticalSeparator  : UIView!
    private var conversationBubble : UITextView!
    private var audioRecorder      : AVAudioRecorder?
    private var audioPlayer        : AVAudioPlayer!
    private var audioPlayerR1      : AVAudioPlayer!
    private var audioPlayerR2      : AVAudioPlayer!
    private var responsePlayer     : AVPlayer?
    public  var userResponse       : String = ""
    public  var aiResponse         : String = ""
    public  var accEmail           : String = ""

    override func viewDidLoad() {
        super.viewDidLoad()
        
        SharedDataModel.shared.accEmail = self.accEmail
        print("accEmail: " + accEmail)
        checkMicrophonePermission()
    }

    override func loadView() {
        super.loadView()
        
        // Initialize micImage
        self.micImage = UIImageView(image: UIImage(named: "Mic_Icon"))
        self.micImage.isUserInteractionEnabled = true
        let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(micLongPress(_:)))
        self.micImage.addGestureRecognizer(longPressRecognizer)
        self.micImage.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(micImage)
        
        // Initialize verticalSeparator
        //self.verticalSeparator = UIView()
        //self.verticalSeparator.translatesAutoresizingMaskIntoConstraints = false
        //self.verticalSeparator.backgroundColor = .red  //for development view only
        //self.view.addSubview(verticalSeparator)
        
        // Initialize micRecStartIcon
        //self.micRecStartIcon = UIImageView(image: UIImage(named: "MicRecStart"))
        //self.micRecStartIcon.isUserInteractionEnabled = true
        //let tapGestureRecognizer1 = UITapGestureRecognizer(target: self, action: #selector(micRecStart(_:)))
        //self.micRecStartIcon.addGestureRecognizer(tapGestureRecognizer1)
        //self.micRecStartIcon.translatesAutoresizingMaskIntoConstraints = false
        //self.view.addSubview(micRecStartIcon)
        
        // Initialize micRecStopIcon
        //self.micRecStopIcon = UIImageView(image: UIImage(named: "MicRecStop"))
        //self.micRecStopIcon.isUserInteractionEnabled = true
        //let tapGestureRecognizer2 = UITapGestureRecognizer(target: self, action: #selector(micRecStop(_:)))
        //self.micRecStopIcon.addGestureRecognizer(tapGestureRecognizer2)
        //self.micRecStopIcon.translatesAutoresizingMaskIntoConstraints = false
        //self.view.addSubview(micRecStopIcon)
        
        // Initialize conversationBubble
        self.conversationBubble = UITextView(frame: CGRect.zero)
        self.conversationBubble.translatesAutoresizingMaskIntoConstraints = false
        self.conversationBubble.isEditable = false
        self.conversationBubble.isScrollEnabled = true
        self.conversationBubble.font = UIFont.boldSystemFont(ofSize: 16)
        self.conversationBubble.backgroundColor = UIColor.lightGray.withAlphaComponent(0.7)
        self.view.addSubview(conversationBubble)
        
        // Set up constraints
        NSLayoutConstraint.activate([
            
            // Constraints for micImage
            self.micImage.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            self.micImage.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -10),
            self.micImage.widthAnchor.constraint(equalToConstant: 110),
            self.micImage.heightAnchor.constraint(equalToConstant: 110),
            
            // Constraints for verticalSeparator
            //self.verticalSeparator.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            //self.verticalSeparator.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -10),
            //self.verticalSeparator.widthAnchor.constraint(equalToConstant: 1),
            //self.verticalSeparator.heightAnchor.constraint(equalToConstant: 110),
            
            // Constraints for micRecStartIcon
            //self.micRecStartIcon.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -10),
            //self.micRecStartIcon.trailingAnchor.constraint(equalTo: verticalSeparator.leadingAnchor, constant: -10),
            //self.micRecStartIcon.widthAnchor.constraint(equalToConstant: 110),
            //self.micRecStartIcon.heightAnchor.constraint(equalToConstant: 110),
            
            // Constraints for micRecStopIcon
            //self.micRecStopIcon.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -10),
            //self.micRecStopIcon.leadingAnchor.constraint(equalTo: verticalSeparator.trailingAnchor, constant: 10),
            //self.micRecStopIcon.widthAnchor.constraint(equalToConstant: 110),
            //self.micRecStopIcon.heightAnchor.constraint(equalToConstant: 110),
            
            // Constraints for conversationBubble
            self.conversationBubble.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            self.conversationBubble.heightAnchor.constraint(equalTo: view.safeAreaLayoutGuide.heightAnchor, multiplier: 0.25),
            //self.conversationBubble.bottomAnchor.constraint(equalTo: verticalSeparator.topAnchor, constant: -10),
            self.conversationBubble.bottomAnchor.constraint(equalTo: micImage.topAnchor, constant: -10),
            self.conversationBubble.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 25),
            self.conversationBubble.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -25),
            
        ]) // end of constraints setup
        
        // Initial greeting
        conversationBubble.text = """
        Rachel: Hello, how may I help you?
        """
        
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        playSFX_Fuuu()
    }

    @objc func micLongPress(_ gesture: UILongPressGestureRecognizer) {
        print("Mic button long press!")
        switch gesture.state {
        case .began:
            print("Mic recording started.")
            playSFX_RobotBlip1()
            //startRecording()
        case .ended:
            print("Mic recording ended.")
            playSFX_RobotBlip2()
            //stopRecording()
        default:
            break
        }
    }

    //@objc func micRecStart(_ gesture: UITapGestureRecognizer) {
    //    print("Mic recording START tapped!")
    //    playSFX_RobotBlip1()
    //    startRecording()
    //}

    //@objc func micRecStop(_ gesture: UITapGestureRecognizer) {
    //    print("Mic recording STOP tapped!")
    //    playSFX_RobotBlip2()
    //    stopRecording()
    //}

    func checkMicrophonePermission() {
        print("Checking for Microphone permission.")
        switch AVAudioSession.sharedInstance().recordPermission {
        case .granted:
            print("    Permission already granted.")
            break
        case .denied:
            print("    User need to enable microphone access in Settings.")
            break
        case .undetermined:
            print("    Requesting permission.")
            AVAudioSession.sharedInstance().requestRecordPermission { granted in
                DispatchQueue.main.async {
                    if granted {
                        // Permission granted, configure the audio session now or when needed.
                        print("        Permission granted.")
                    } else {
                        // Permission denied, inform the user.
                        print("        Permission denied.")
                    }
                }
            }
        @unknown default:
            break
        }
    }

    func configureAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playAndRecord, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Failed to configure audio session: \(error)")
        }
    }

    func startRecording() {
        configureAudioSession()
        
        let audioFilename = getDocumentsDirectory().appendingPathComponent("recording.wav")
        let settings = [
            AVFormatIDKey: Int(kAudioFormatLinearPCM),
            AVSampleRateKey: 44100,
            AVNumberOfChannelsKey: 1,
            AVLinearPCMBitDepthKey: 16,
            AVLinearPCMIsBigEndianKey: false,
            AVLinearPCMIsFloatKey: false,
            AVEncoderAudioQualityKey: AVAudioQuality.high.rawValue
        ] as [String : Any]
        
        do {
            audioRecorder = try AVAudioRecorder(url: audioFilename, settings: settings)
            audioRecorder?.delegate = self
            audioRecorder?.record()
        } catch {
            print("Failed to start recording: \(error)")
        }
    }

    func stopRecording() {
        audioRecorder?.stop()
        audioRecorder = nil
        uploadAudioFile()
    }

    func getDocumentsDirectory() -> URL {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        return paths[0]
    }

    func uploadAudioFile() {
        // http://127.0.0.1:5000 (at localhost)
        // https://c6c8-99-6-249-142.ngrok-free.app (using ngrok, link changes everytime server is restarted)
        let url = URL(string: "https://90a3-2600-1700-6bf1-853f-d136-5eaa-46a6-fe8f.ngrok-free.app/process-audio")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        
        let boundary = "Boundary-\(UUID().uuidString)"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        let audioFilename = getDocumentsDirectory().appendingPathComponent("recording.wav")
        var data = Data()
        
        data.append("\r\n--\(boundary)\r\n".data(using: .utf8)!)
        data.append("Content-Disposition: form-data; name=\"file\"; filename=\"recording.wav\"\r\n".data(using: .utf8)!)
        data.append("Content-Type: audio/wav\r\n\r\n".data(using: .utf8)!)
        data.append(try! Data(contentsOf: audioFilename))
        data.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        
        let task = URLSession.shared.uploadTask(with: request, from: data) { data, response, error in
            guard let data = data, error == nil else {
                print("Error during the request: \(error?.localizedDescription ?? "No error description")")
                return
            }
            
            if let responseJSON = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any],
               let correctedTranscription = responseJSON["corrected_transcription"] as? String,
               let responseText = responseJSON["response_text"] as? String,
               let responseAudioURLString = responseJSON["response_audio_url"] as? String,
               let responseAudioURL = URL(string: responseAudioURLString) {
                
                DispatchQueue.main.async { [weak self] in
                    self?.conversationBubble.text = """
                    You: \(correctedTranscription)
                    
                    Rachel: \(responseText)
                    """
                    
                    print("userResponse: " + correctedTranscription)
                    print("aiResponse: " + responseText)
                    print("responseAudioURLString: " + responseAudioURLString)
                    self?.userResponse = correctedTranscription
                    self?.aiResponse = responseText
                    self?.saveMessage(userEmail: self?.accEmail ?? "no_useremail", role: "user", content: correctedTranscription)
                    self?.saveMessage(userEmail: self?.accEmail ?? "no_useremail", role: "assistant", content: responseText)
                    
                    // Play streamed audio response from the URL
                    self?.playStreamedAudio(from: responseAudioURL)
                }
            }
        }
        task.resume()
    }

    func playStreamedAudio(from url: URL) {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
            
            // Initialize and play the audio from the URL
            responsePlayer = AVPlayer(url: url)
            responsePlayer?.play()
        } catch {
            print("Failed to initialize AVAudioSession: \(error.localizedDescription)")
        }
    }

    func saveMessage(userEmail: String, role: String, content: String) {
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else { return }
        let managedContext = appDelegate.persistentContainer.viewContext
        let entity = NSEntityDescription.entity(forEntityName: "Message", in: managedContext)!
        let message = NSManagedObject(entity: entity, insertInto: managedContext)
        
        message.setValue(userEmail, forKeyPath: "useremail")
        message.setValue(role, forKey: "role")
        message.setValue(content, forKey: "content")
        message.setValue(Date(), forKey: "timestamp")
        
        do {
            try managedContext.save()
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
        print("Executed function saveMessage.")
    }

    func playSFX_RobotBlip1() {
        if let audioPath = Bundle.main.path(forResource: "RobotBlip1", ofType: "mp3") {
            do {
                let audioURL = URL(fileURLWithPath: audioPath)
                self.audioPlayerR1 = try AVAudioPlayer(contentsOf: audioURL)
                self.audioPlayerR1.delegate = self
                self.audioPlayerR1.volume = 1
                self.audioPlayerR1.currentTime = 0
                self.audioPlayerR1.prepareToPlay()
                self.audioPlayerR1.play()
            } catch {
                print("Failed to play RobotBlip1 sound: \(error)")
            }
        }
    }

    func playSFX_RobotBlip2() {
        if let audioPath = Bundle.main.path(forResource: "RobotBlip2", ofType: "mp3") {
            do {
                let audioURL = URL(fileURLWithPath: audioPath)
                self.audioPlayerR2 = try AVAudioPlayer(contentsOf: audioURL)
                self.audioPlayerR2.delegate = self
                self.audioPlayerR2.volume = 1
                self.audioPlayerR2.currentTime = 0
                self.audioPlayerR2.prepareToPlay()
                self.audioPlayerR2.play()
            } catch {
                print("Failed to play RobotBlip2 sound: \(error)")
            }
        }
    }

    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        if flag {
            if player == audioPlayerR1 {
                startRecording()
            } else if player == audioPlayerR2 {
                stopRecording()
            } else {
                // do nothing.
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
                print("Failed to play Fuuu sound: \(error)")
            }
        }
    }

}
