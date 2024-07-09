from flask import Flask, request, jsonify, send_from_directory, url_for
from openai import OpenAI
from dotenv import load_dotenv
import os
#from catboost import CatBoostClassifier
import json

load_dotenv()

app = Flask(__name__)
client = OpenAI(api_key=os.getenv('OPENAI_API_KEY'))
#cb_classifier = CatBoostClassifier()
#cb_classifier.load_model('./model/model_cb.dump')

system_prompt1 = "You are a helpful assistant that speaks only in English. Your task is to correct any spelling discrepancies in the transcribed text. Only add necessary punctuation such as periods, commas, and capitalization, and use only the context provided."
system_prompt2 = "You are a close friend that speaks only in English, a female named Rachel, you are a companion who cares for people and helps to watch out for mental health issues and signs of dementia."

@app.route('/process-audio', methods=['POST'])
def voice_analysis():
    # Check if the request has an audio file
    if 'audio' not in request.files:
        return jsonify({"error": "No audio file provided"}), 400

    audio_file = request.files['audio']
    audio_path = "./audio/test.m4a"
    audio_file.save(audio_path)

    # Open and Process the Audio File
    original_transcript = process_audio(audio_path)
    print(original_transcript)

    # Corrected Transcript
    corrected_transcript_response = correct_transcript(original_transcript)

    if 'user_id' not in request.form:
        return jsonify({"error": "No user id provided"}), 400
    
    # Get Past Message History for Context
    user_id = request.form.get("user_id")

    ''' Used for CatBoost Model Dementia Analysis
    text_embedding_response = dementia_analysis_embedding(original_transcript)
    embedding = [text_embedding_response]
    prediction = cb_classifier.predict(embedding)
    cat_boost_dementia_analysis = "Dementia" if prediction[0] == 1 else "No Dementia"
    print(cat_boost_dementia_analysis)
    '''

    # Dementia Analysis utilizing trained GPT Fine-Tuned Model
    dementia_analysis = dementia_analysis_ft_model(original_transcript)
    print(dementia_analysis)

    dementia_analysis = True if dementia_analysis == "Dementia" else False

    # Get AI's responses
    corrected_conversation = load_corrected_conversation_history(user_id)
    ai_text_response = get_ai_text_response(corrected_conversation, corrected_transcript_response)

    # Add Messages to Conversation History
    save_original_messages(user_id, [{"role": "user", "content": original_transcript}, {"role": "assistant", "content": ai_text_response}])
    save_corrected_messages(user_id, [{"role": "user", "content": corrected_transcript_response}, {"role": "assistant", "content": ai_text_response}])

    ai_audio_response = get_ai_audio_response(ai_text_response)

    output_path = 'audio/response.mp3'
    with open(output_path, 'wb') as file:
        for chunk in ai_audio_response.iter_bytes(chunk_size=1024):
            file.write(chunk)

    audio_url = url_for('serve_audio', filename="response.mp3", _external=True)

    return jsonify({
        "corrected_transcript": corrected_transcript_response,
        "response_text": ai_text_response,
        "response_audio_url": audio_url,
        "dementia_analysis": dementia_analysis
    })

@app.route('/audio/<filename>')
def serve_audio(filename):
    return send_from_directory('audio', filename)

'''

    Auxillary Function Declarations

'''

def load_corrected_conversation_history(user_id):
    try:
        with open(f"./data/corrected_conversation_history.json", 'r') as f:
            user_message_history = json.load(f)
            return user_message_history.get(user_id, [])
    except FileNotFoundError:
        print("The file conversation_history.json does not exist.")

def load_original_conversation_history(user_id):
    try:
        with open(f"./data/conversation_history.json", 'r') as f:
            user_message_history = json.load(f)
            return user_message_history.get(user_id, [])
    except FileNotFoundError:
        print("The file conversation_history.json does not exist.")

def save_original_messages(user_id, messages):
    try:
        file_path = f"./data/corrected_conversation_history.json"
        data = {}
        if os.path.exists(file_path):
            with open(file_path, 'r') as f:
                data = json.load(f)
        
        if user_id in data:
            data[user_id].extend(messages)
        else:
            data[user_id] = messages

        with open(file_path, 'w') as f:
            json.dump(data, f)

    except json.JSONDecodeError:
        print(f"Error decoding JSON from {file_path}. Check for file corruption.")
    except FileNotFoundError:
        print(f"The file {file_path} does not exist and failed to create.")
    except Exception as e:
        print(f"Failed to save messages: {e}")

def save_corrected_messages(user_id, messages):
    file_path = f"./data/conversation_history.json"
    try:
        data = {}
        if os.path.exists(file_path):
            with open(file_path, 'r') as f:
                data = json.load(f)
        
        if user_id in data:
            data[user_id].extend(messages)
        else:
            data[user_id] = messages

        with open(file_path, 'w') as f:
            json.dump(data, f)

    except json.JSONDecodeError:
        print(f"Error decoding JSON from {file_path}. Check for file corruption.")
    except FileNotFoundError:
        print(f"The file {file_path} does not exist and failed to create.")
    except Exception as e:
        print(f"Failed to save messages: {e}")

def process_audio(audio_path):
    with open(audio_path, "rb") as file:
        transcript = client.audio.transcriptions.create(
            model="whisper-1",
            file=file,
            response_format="text"
        )
    return transcript

def correct_transcript(original_transcript):
    transcript = client.chat.completions.create(
        model="gpt-4-turbo",
        temperature=0,
        messages=[
            {"role": "system", "content": system_prompt1},
            {"role": "user", "content": original_transcript}
        ]
    ).choices[0].message.content.strip()
    return transcript

''' Used for Cat Boost Model
def dementia_analysis_embedding(transcript):
        # Use the original transcript of user's voice for the most accuracy for dementia detection
        text_embedding = client.embeddings.create(
            model="text-embedding-3-large",
            input=transcript
        ).data[0].embedding
        return text_embedding
        '''

def dementia_analysis_ft_model(transcript):
    result = client.chat.completions.create(
        model="ft:gpt-3.5-turbo-0125:personal::9GO0KQpC",
        messages=[
                  {"role": "system", "content": f"Transcript: ${transcript}"}
                  ],
        temperature=0
    ).choices[0].message.content.strip()
    return result


def get_ai_text_response(message_history, transcript):

    if (message_history):
        conversation = [{"role": "system", "content": system_prompt2}] + message_history + [{"role":"user", "content": transcript}]
    else:
        conversation = [
            {"role": "system", "content": system_prompt2},
            {"role":"user", "content": transcript}
        ]

    ai_text = client.chat.completions.create(
        model="gpt-4-turbo",
        temperature=0.7,
        messages=conversation
        ).choices[0].message.content.strip()
    return ai_text

def get_ai_audio_response(ai_text):
    ai_audio = client.audio.speech.create(
        model="tts-1",
        voice="nova",
        input=ai_text
    )
    return ai_audio
    
if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0")
