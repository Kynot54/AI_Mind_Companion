from flask import Flask, request, jsonify, send_from_directory
from werkzeug.utils import secure_filename
import os
import json
from openai import OpenAI
from pathlib import Path

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = os.path.join(os.getcwd(), 'uploads')  # Ensure absolute path
app.config['MAX_CONTENT_LENGTH'] = 24 * 1024 * 1024  # 24 MB upload limit
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)  # Ensure the upload directory exists

# OPENAI_API_KEY should be set as Environment Variable in macOS,
# this is to protect the the API key from being exposed publicly.
openai_client = OpenAI(api_key=os.environ.get('OPENAI_API_KEY'))

system_prompt1 = "You are a helpful assistant that speaks only in English. Your task is to correct any spelling discrepancies in the transcribed text. Only add necessary punctuation such as periods, commas, and capitalization, and use only the context provided."
system_prompt2 = """You are a close friend that speaks only in English, a female named Rachel, you are a companion who cares for people and helps to watch out for mental health issues and signs of dementia.
If you realize that the user you are talking with is having signs of dementia, such as signs of memory loss, signs of confusion, saying nonsensical things, and longer pauses when speaking, then you should let the user know explicitly that he could be showing signs of dementia, that he might be suffering from dementia, and that it would be good to look for medical help."""
system_prompt3 = """You are a close friend that speaks only in English, a female named Rachel, you are a companion who cares for people and helps to watch out for mental health issues and signs of dementia.
Assume that the user you are talking with is confirmed to have dementia, then you should let the user know explicitly that he could be showing signs of dementia, that he might be suffering from dementia, and that it would be good to look for medical help."""

# Function that transcribes the audio into text with Whisper model
def transcribe_audio_with_whisper(audio_path):
    with open(audio_path, "rb") as audio_file:
        transcription = openai_client.audio.transcriptions.create(
            model="whisper-1",
            file=audio_file
        )
    print("[API] Transcribed audio with Whisper: " + transcription.text)
    return transcription.text

# Function that corrects the transcribed text with context concordance with GPT model
def generate_corrected_transcript(original_transcript):
    response = openai_client.chat.completions.create(
        model="gpt-4-turbo",  #gpt-4-turbo #gpt-3.5-turbo-0125
        temperature=0,
        messages=[
            {"role": "system", "content": system_prompt1},
            {"role": "user", "content": original_transcript}
        ]
    )
    corrected_whisper_transcript = response.choices[0].message.content
    print("[API] Corrected Whisper transcript by GPT: " + corrected_whisper_transcript)
    return corrected_whisper_transcript

# Function that calls GPT AI model to generate a response to the  user's input (corrected transcript)
def generate_response(corrected_transcript):
    # Load existing conversation history
    history_path = Path('uploads/conversation_history.json')
    if history_path.exists():
        with open(history_path, 'r') as file:
            history_messages = json.load(file)
    else:
        history_messages = []
    # Create messages for OpenAI request including history
    conversation = [
        {"role": "system", "content": system_prompt2}
    ] + history_messages + [
        {"role": "user", "content": corrected_transcript}
    ]
    # Request response from GPT AI model
    response = openai_client.chat.completions.create(
        model="gpt-4-turbo",  #gpt-4-turbo #gpt-3.5-turbo-0125
        temperature=0.7,
        #max_tokens=150
        messages=conversation
    )
    response_text = response.choices[0].message.content
    print("[API] Response text by GPT (no dementia detected): " + response_text)
    append_to_conversation_history(corrected_transcript, response_text)
    return response_text

def generate_response_d(corrected_transcript):
    # Load existing conversation history
    history_path = Path('uploads/conversation_history.json')
    if history_path.exists():
        with open(history_path, 'r') as file:
            history_messages = json.load(file)
    else:
        history_messages = []
    # Create messages for OpenAI request including history
    conversation = [
        {"role": "system", "content": system_prompt3}
    ] + history_messages + [
        {"role": "user", "content": corrected_transcript}
    ]
    # Request response from GPT AI model
    response = openai_client.chat.completions.create(
        model="gpt-4-turbo",  #gpt-4-turbo #gpt-3.5-turbo-0125
        temperature=0.7,
        #max_tokens=150
        messages=conversation
    )
    response_text = response.choices[0].message.content
    print("[API] Response text by GPT (dementia detected): " + response_text)
    append_to_conversation_history(corrected_transcript, response_text)
    return response_text

# Function that converts the text into audio with TTS model
def convert_text_to_speech(text, output_path):
    response = openai_client.audio.speech.create(
        model="tts-1",
        voice="nova",
        input=text
    )
    print("[API] Converted GPT response text to audio by TTS.")
    response.stream_to_file(output_path)

# Function that adds new messages to conversation history
def append_to_conversation_history(user_input, assistant_response):
    history_path = Path('uploads/conversation_history.json')
    # Check if the history file exists, if not, create it with an empty list
    if not history_path.exists():
        history_path.write_text(json.dumps([]))
    # Read the existing history
    with open(history_path, 'r+') as file:
        history = json.load(file)
        # Append new messages
        history.append({"role": "user", "content": user_input})
        history.append({"role": "assistant", "content": assistant_response})
        # Move to the beginning of the file to overwrite
        file.seek(0)
        # Save the updated history
        json.dump(history, file)
        # Truncate the file in case new data is shorter than old data
        file.truncate()
        print("Conversation history updated.")

# Function that calls the fine-tuned GPT model for dementia classification
def dementia_analysis_ft_model(transcript):
    result = openai_client.chat.completions.create(
        model="ft:gpt-3.5-turbo-0125:personal:ai-mind-companion:9H1fbEoT",
        messages=[
                  {"role": "system", "content": f"Transcript: ${transcript}"}
                  ],
        temperature=2.0
    ).choices[0].message.content.strip()
    print("[API] [T=2.0] Classified transcript with Fine-tunned GPT model for dementia status.")
    return result

@app.route('/process-audio', methods=['POST'])
def process_audio():
    print("|===== Audio processing request received =====|")
    if 'file' not in request.files:
        return jsonify({'error': 'No file part'}), 400
    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
    
    filename = secure_filename(file.filename)
    filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    file.save(filepath)
    
    # Transcribe audio to text with Whisper
    #print("Calling function for transcribing audio to text.")
    transcription = transcribe_audio_with_whisper(filepath)
    
    # Generate corrected transcript using GPT
    #print("Calling function for generating corrected transcript.")
    corrected_transcription = generate_corrected_transcript(transcription)
    
    # Call the fine-tuned GTP AI model for classification of dementia
    dementia_analysis = dementia_analysis_ft_model(transcription)
    print("Result of Dementia analysis: " + dementia_analysis)
    
    # Generate GPT's response to the corrected transcription
    #print("Calling function for generating GPT's response.")
    if dementia_analysis == "Dementia":
        response_text = generate_response_d(corrected_transcription)
        #print("Executed function generate_response_d()")
    else:
        response_text = generate_response(corrected_transcription)
        #print("Executed function generate_response()")
    
    # Convert GPT response to speech
    #print("Calling function for converting generated response to speech.")
    response_audio_path = os.path.join(app.config['UPLOAD_FOLDER'], 'response.wav')
    convert_text_to_speech(response_text, response_audio_path)
    
    # Clean up the original audio file
    #os.remove(filepath)
    
    print("|===== Finished processing audio and returning results =====|")
    return jsonify({
        'transcription': transcription,
        'corrected_transcription': corrected_transcription,
        'response_text': response_text,
        'response_audio_url': request.host_url + 'uploads/response.wav'
    }), 200

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    # Serve files from the upload folder.
    print("|===== Audio response transfer =====|")
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

@app.route('/clear-history', methods=['POST'])
def clear_history():
    history_path = Path('uploads/conversation_history.json')
    # Resets the conversation history
    print("|===== Conversation history reseted =====|")
    with open(history_path, 'w') as file:
        json.dump([], file)
    return jsonify({"status": "success", "message": "Conversation history cleared"}), 200

@app.route('/')
def hello_world():
    print("Hello, World!")
    return 'Hello, World!'

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)  # Listen on all network interfaces
