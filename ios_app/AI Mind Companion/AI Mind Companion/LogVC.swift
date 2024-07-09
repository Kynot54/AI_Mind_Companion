//
//  LogVC.swift
//  AI Mind Companion
//
//  Created by Josh Tai on 2/22/24.
//

import Foundation
import UIKit
import AVFoundation
import AVKit
import CoreData

class LogVC: UIViewController {

    @IBOutlet var titleLabel          : UILabel!
    @IBOutlet var clearHistoryButton  : UIButton!
    private   var conversationHistory : UITextView!
    public    var accEmail            : String = ""
    private   var audioPlayer         : AVAudioPlayer!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        accEmail = SharedDataModel.shared.accEmail ?? ""
        print("accEmail: " + accEmail)
    }

    override func loadView() {
        super.loadView()
        
        // Initialize conversationHistory
        self.conversationHistory = UITextView(frame: CGRect.zero)
        self.conversationHistory.translatesAutoresizingMaskIntoConstraints = false
        self.conversationHistory.isEditable = false
        self.conversationHistory.isScrollEnabled = true
        self.conversationHistory.font = UIFont.boldSystemFont(ofSize: 16)
        self.conversationHistory.backgroundColor = UIColor.lightGray.withAlphaComponent(0.7)
        self.view.addSubview(conversationHistory)
        
        // Set up constraints
        NSLayoutConstraint.activate([
            
            // Constraints for conversationBubble
            self.conversationHistory.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            self.conversationHistory.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 50),
            self.conversationHistory.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -25),
            self.conversationHistory.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 25),
            self.conversationHistory.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -25),
            
        ]) // end of constraints setup
        
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        playSFX_Fuuu()
        fetchMessages()
        scrollToBottom()
    }

    @IBAction func clearHistoryButtonTapped(_ sender: UIButton) {
        playSFX_ButtonClick()
        confirmAndDeleteMessagesForCurrentUser()
    }

    func fetchMessages() {
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else { return }
        let managedContext = appDelegate.persistentContainer.viewContext
        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: "Message")
        
        // Filter messages by the current user's email
        fetchRequest.predicate = NSPredicate(format: "useremail == %@", accEmail)
        
        // Sort the messages by timestamp
        let sortDescriptor = NSSortDescriptor(key: "timestamp", ascending: true)
        fetchRequest.sortDescriptors = [sortDescriptor]
        
        do {
            let messages = try managedContext.fetch(fetchRequest)
            updateConversationHistory(with: messages)
        } catch let error as NSError {
            print("Could not fetch. \(error), \(error.userInfo)")
        }
        
        print("Executed function fetchMessages.")
    }

    func updateConversationHistory(with messages: [NSManagedObject]) {
        var conversationText = ""
        for message in messages {
            if let role = message.value(forKey: "role") as? String,
               let content = message.value(forKey: "content") as? String {
                let prefix = role == "user" ? "You: " : "\(role.capitalized): "
                conversationText += prefix + content + "\n\n"
            }
        }
        
        DispatchQueue.main.async {
            self.conversationHistory.text = conversationText
        }
        
        print("Executed function updateConversationHistory.")
    }

    func confirmAndDeleteMessagesForCurrentUser() {
        // Create the alert controller
        let alert = UIAlertController(title: "Confirm Deletion", message: "Are you sure you want to delete all messages for this user?", preferredStyle: .alert)
        
        // Add a "Cancel" action
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        
        // Add a "Delete" action
        alert.addAction(UIAlertAction(title: "Delete", style: .destructive, handler: { [weak self] _ in
            // Call the function to delete messages
            self?.deleteMessagesForCurrentUser()
            self?.conversationHistory.text = ""
        }))
        
        // Present the alert
        playSFX_Error()
        self.present(alert, animated: true, completion: nil)
    }

    func deleteMessagesForCurrentUser() {
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else { return }
        let managedContext = appDelegate.persistentContainer.viewContext
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Message")
        fetchRequest.predicate = NSPredicate(format: "useremail == %@", accEmail)
        
        do {
            let fetchResults = try managedContext.fetch(fetchRequest)
            for object in fetchResults {
                guard let objectData = object as? NSManagedObject else {continue}
                managedContext.delete(objectData)
            }
            // Save Changes
            try managedContext.save()
        } catch let error as NSError {
            print("Detele all data in \(accEmail) error : \(error) \(error.userInfo)")
        }
        
        // Clear conversation history from backend server
        // http://127.0.0.1:5000/clear-history (at localhost)
        // https://c6c8-99-6-249-142.ngrok-free.app (using ngrok, link changes everytime server is restarted)
        guard let url = URL(string: "https://90a3-2600-1700-6bf1-853f-d136-5eaa-46a6-fe8f.ngrok-free.app/clear-history") else { return }
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            
            let task = URLSession.shared.dataTask(with: request) { data, response, error in
                guard let _ = data, error == nil else {
                    print("Error: \(error?.localizedDescription ?? "No error description")")
                    return
                }
                if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 {
                    print("History cleared successfully.")
                } else {
                    print("Failed to clear history.")
                }
            }
            task.resume()
        
        print("Executed function deleteMessagesForCurrentUser.")
    }

    func scrollToBottom() {
        DispatchQueue.main.async { [weak self] in
            guard let strongSelf = self else { return }
            if strongSelf.conversationHistory.text.count > 0 {
                let location = strongSelf.conversationHistory.text.count - 1
                let bottom = NSMakeRange(location, 1)
                strongSelf.conversationHistory.scrollRangeToVisible(bottom)
            }
        }
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
                print("Failed to play ButtonClick sound.")
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
                print("Failed to play Error sound.")
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
