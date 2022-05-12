import os
import pandas as pd 
from google.cloud import texttospeech

file_path = os.path.dirname(os.path.realpath(__file__))

os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = f"{file_path}\\authkey.json"

client = texttospeech.TextToSpeechClient()

def google_tts(quote):
    print("tts ing...")
    
    synthesis_input = texttospeech.SynthesisInput(text=quote)

    voice = texttospeech.VoiceSelectionParams(
        language_code="en-US", ssml_gender=texttospeech.SsmlVoiceGender.FEMALE, name = "en-US-Wavenet-G"
    )

    # Select the type of audio file you want returned
    audio_config = texttospeech.AudioConfig(
        # https://cloud.google.com/text-to-speech/docs/reference/rpc/google.cloud.texttospeech.v1#audioencoding
        audio_encoding=texttospeech.AudioEncoding.MP3,
        speaking_rate = 0.75
        # speaking_rate = 2
    )

    response = client.synthesize_speech(
        input=synthesis_input, voice=voice, audio_config=audio_config
    )

    # save the file to local storage
    with open(r"D:\COMP FYP\sound\output.mp3", "wb") as out:
        out.write(response.audio_content)
        print('Audio content written to file "output.mp3"')

    url = upload_to_bucket("audio/question.mp3",r"D:\COMP FYP\sound\output.mp3","adept-ethos-339308.appspot.com")

    return url

from google.cloud import speech_v1p1beta1 as speech

speech_client = speech.SpeechClient()

config_mp3 = speech.RecognitionConfig(
    encoding=speech.RecognitionConfig.AudioEncoding.MP3,
    sample_rate_hertz=16000,
    # sample_rate_hertz=48000,
    language_code='en-US',
    enable_automatic_punctuation=True,
    use_enhanced=True,
    audio_channel_count=2,
    # A model must be specified to use enhanced model.
    model="phone_call"
)

config_wav = speech.RecognitionConfig(
    encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
    sample_rate_hertz=44100,
    enable_automatic_punctuation=True,
    language_code='en-US',
    audio_channel_count=2,
    use_enhanced=True,
    model="phone_call"
)

def google_stt(uri):
    print("stt ing...")
    # media_uri = 'gs://speech-to-text-testing1/321go.mp3'
    media_uri = uri
    long_audi = speech.RecognitionAudio(uri=media_uri)
    
    operation = speech_client.long_running_recognize(
        config=config_wav,
        audio=long_audi,
    )

    # response = operation.result(timeout=90)
    response = operation.result()

    print("finish stt...")
    full_text_list = ""
    for result in response.results:
        full_text_list += result.alternatives[0].transcript
    #     print(result.alternatives[0].transcript)
    #     print(result.alternatives[0].confidence)
    #     print()

    return full_text_list

from google.cloud import storage

def upload_to_bucket(blob_name, path_to_file, bucket_name):
    """ Upload data to a bucket"""

    storage_client = storage.Client.from_service_account_json(
        f"{file_path}\\authkey.json")

    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.blob(blob_name)
    blob.upload_from_filename(path_to_file)

    blob.reload()
    while blob.updated is None:
        blob.reload()
    return blob.public_url

if __name__ == "__main__":
    
    url = google_tts("Hello Peter. Nice to meet you. I am your interviewer.")
    print(url)

    text = google_stt("gs://adept-ethos-339308.appspot.com/audio/answer.wav")
    print(text)
    pass