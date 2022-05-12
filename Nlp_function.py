from transformers import pipeline
classifier = pipeline("zero-shot-classification",model="facebook/bart-large-mnli")

summarizer = pipeline("summarization", model="facebook/bart-large-cnn")

import spacy
# nlp = spacy.load("en_core_web_sm")
# nlp = spacy.load("en_core_web_lg")
nlp = spacy.load("en_core_sci_lg")

if __name__ == "__main__":
    
    # sequence_to_classify = "I love coding and i know python."
    # candidate_labels = ["PHP", "RESTful API", "AWS Cloud Service", "Docker", "e-Payment", "e-Commerce", "web/mobile applications", "Jenkins", "MSSQL", "repeat", "pardon", "not sure"]
    # result = classifier(sequence_to_classify, candidate_labels, multi_label = True)
    # print(result)

    # text ="The tower is 324 metres (1,063 ft) tall, about the same height as an 81-storey building, and the tallest structure in Paris. Its base is square, measuring 125 metres (410 ft) on each side. During its construction, the Eiffel Tower surpassed the Washington Monument to become the tallest man-made structure in the world, a title it held for 41 years until the Chrysler Building in New York City was finished in 1930. It was the first structure to reach a height of 300 metres. Due to the addition of a broadcasting aerial at the top of the tower in 1957, it is now taller than the Chrysler Building by 5.2 metres (17 ft). Excluding transmitters, the Eiffel Tower is the second tallest free-standing structure in France after the Millau Viaduct."
    # text = "Taking car as an example, There will be a class of car. The property of a car might be brand, gear, engine, seat numbers, driver, wheels, which could be different data types. For the method of a car, it could be accelerate, brake, wipe, honk. Talk with inheritance, the sub classes could extend the car class. And override the methods, or have more property or methods. The sub classes could be vehicle, motorcycle, van or any other types of car in the real world. "
    text = "There will be a class of car. The property of a car might be brand, gear, engine, seat numbers, driver, wheels, which could be different data types. For the method of"
    # result = summarizer(text, max_length=150, min_length=30, do_sample=False)

    summarizer_rate = 0.5
    summarizer_words_limit = 60
    num_text = len(text.split())

    result = summarizer(text, max_length=int(num_text * summarizer_rate), min_length=int(summarizer_words_limit * summarizer_rate),do_sample=False)
    print(result)

    print("-"*20)

    # doc = nlp(text)
    text_list = [ent.text for ent in list(nlp(text).ents)]
    print(text_list)

    pass
