package com.github.ai_mind_companion.app.shared

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.ai_mind_companion.app.utils.AudioRecorder
import com.github.ai_mind_companion.app.utils.Message
import com.github.ai_mind_companion.app.utils.MessageType
import com.github.ai_mind_companion.app.utils.TrustedContact
import com.github.ai_mind_companion.app.utils.sendAudioFileToServer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.util.Locale


class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var audioRecorder: AudioRecorder

    fun setAudioRecorder(audioRecorder: AudioRecorder) {
        this.audioRecorder = audioRecorder
    }

    var messages = mutableStateListOf<Message>()
        private set

    private val intent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak naturally and clearly.")
        }
    }

    fun initSpeechRecognizer(speechRecognizer: SpeechRecognizer?) {
        this.speechRecognizer = speechRecognizer
        speechRecognizer?.setRecognitionListener(object: RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { recordedText ->
                    Log.d(TAG,"Recorded Message $recordedText")
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startSpeechToText() {
        speechRecognizer?.startListening(intent)
    }

    fun stopSpeechToText() {
        speechRecognizer?.stopListening()
    }

    override fun onCleared() {
        speechRecognizer?.destroy()
    }

    fun startRecording() {
        try {
            audioRecorder.startRecording()
        } catch(e:Exception) {
                Log.w("An Error has occured in start recording", e)
            }
    }

    fun stopRecording(): File? {
        return try {
            audioRecorder.stopRecording()
        } catch(e:Exception) {
            Log.w("An Error has occured in start recording", e)
            null
        }
    }

    fun sendAudioFileToServer(audioFile: File) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            Log.d(TAG, "User ID: $userId")
            sendAudioFileToServer(
                audioFile,
                userId,
                "http://10.0.2.2:5000/process-audio"
            ) { response ->
                val messageResponses = response.split('~')
                Log.d(
                    TAG,
                    "MessageResponses: ${messageResponses[0] + ' ' + messageResponses[1] + messageResponses[2]}"
                )
                val userMessage = Message(
                    author = "User",
                    body = messageResponses[0],
                    type = MessageType.USER
                )

                val botMessage = Message(
                    author = "Bot",
                    body = messageResponses[1],
                    type = MessageType.BOT
                )

                /* DEBUG:
                    val messageAnalysisBool = messageResponses[2].toBooleanStrict()
                    Log.d(TAG,"Analysis Bool Value $messageAnalysisBool")
                */

                messages.add(userMessage)
                messages.add(botMessage)
                saveMessage(userMessage, messageResponses[2].toBooleanStrict())
                saveMessage(botMessage, null)
            }
        }
    }

    private fun saveMessage(messageContent: Message, analysis: Boolean?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val collectionPath = "users/$userId/messages"

        db.collection(collectionPath).add(messageContent)
            .addOnSuccessListener { documentReference ->
                Log.d("Fire-store", "Document Snapshot written with ID: ${documentReference.id}")
                if (analysis == true)
                    updateDocumentCounts(userId)
            }.addOnFailureListener{ e ->
                Log.w("Fire-store", "Error Adding Document", e)
            }
    }

    private fun updateDocumentCounts(userId: String?) {
        if (userId != null) {
            val docRef = db.collection("users").document("$userId").collection("count")
            docRef.whereNotEqualTo("count", null).get().addOnSuccessListener { querySnapShot ->
                if (!querySnapShot.isEmpty) {
                    val documentToUpdateReference = querySnapShot.documents[0]
                    val currentCount = documentToUpdateReference.getLong("count") ?: 0
                    val newCount = currentCount + 1
                    val newDementiaScore = newCount / 100.00
                    val updates = mapOf(
                        "count" to newCount,
                        "dementia_score" to newDementiaScore
                    )
                    documentToUpdateReference.reference.update(updates).addOnSuccessListener {
                        Log.d(TAG,"Document Updated Successfully")
                    }.addOnFailureListener { e ->
                        Log.d(TAG,"Document Updated Successfully", e)
                    }
                } else {
                    val newDocReference = docRef.document()
                    val initialDementiaScore = 1 / 100.00
                    val data = mapOf(
                        "count" to 1,
                        "dementia_score" to initialDementiaScore
                    )
                    newDocReference.set(data)
                        .addOnSuccessListener {
                            Log.d(TAG,"Document Successfully Created and Count Set")
                        }.addOnFailureListener { e ->
                            Log.w(TAG, "Document Failed to Be Updated", e)
                        }
                }
            }.addOnFailureListener {
                Log.w(TAG, "Document Failed to Be Created")
            }
        }
    }

    fun fetchMessages(userId: String) {
        val collectionPath = "users/$userId/messages"

        db.collection(collectionPath)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener addSnapshotListener@{ snapshots, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                try {
                    val messageList = snapshots?.documents?.mapNotNull { document ->
                        document.toObject(Message::class.java)
                    }
                    messages.clear()
                    if (messageList != null) {
                        messages.addAll(messageList)
                    }
                }
                catch (e: Exception) {
                    Log.e("Error Loading Messages", e.toString())
                }
            }
    }

    fun fetchUserId():String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun deleteMessages(userId: String) {
        val collectionPath = "users/$userId/messages"
        try {
            db.collection(collectionPath)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        db.collection(collectionPath).document(document.id).delete()
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot ${document.id}successfully deleted!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG,"Error Deleting Document: ${document.id}", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG,"Error Getting Documents: ", e)
                }
        }
        catch (e: Exception) {
            Log.w(TAG,"Error Deleting Document", e)
        }
    }

    val contactName = mutableStateOf("")
    val contactEmail = mutableStateOf("")
    fun addTrustedContact(contact: TrustedContact) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).collection("trustedContacts").add(contact)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        "Fire-store",
                        "Document Snapshot written with ID: ${documentReference.id}"
                    )

                }.addOnFailureListener { e ->
                    Log.w("Fire-store", "Error Adding Document", e)
                }
        }
    }
}