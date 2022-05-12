import logging, time, json
from Report import gen_report
from Question import Questionlv0
from ScoringModel import ScoringModel
from Google_cloud_api import google_tts
from Mongodb import insert_report

json_type_q = "question"
json_type_c = "comment"
json_type_f = "finish"
questions_dict = {}
photoURLs = []
repeat_labels = ["ask me to repeat"]
not_sure_labels = ["not sure"]

sessions = ["greeting", "work_exp", "tech_skill", "bye"]
dont_understand_dict = {"greeting":0, "work_exp":0, "tech_skill":0, "bye":0}
repeat_dict = {"greeting":0, "work_exp":0, "tech_skill":0, "bye":0}
topics_asked = {"PHP":0, "RESTful API":0, "O O P":0, "multithreading":0, "Spring Framework":0, "e-Payment":0, "nodejs": 0 ,"web/mobile applications":0, "Jenkins":0, "SQL":0}
# topics_asked = {"SQL":0, "Python":0, "Data Scraping":0, "Machine Learning":0, "Database":0}


def json_to_client(type, url, photo_required = "false"):
    return bytes(json.dumps({"type":type ,"url":url, "photo": photo_required}),encoding="utf-8")

def get_topics_greeting(topics):
    print("checking topic [greeting]")
    if topics['labels'][0] in ['confirm','greeting']:
        return "OK. Let's start our interview."

def get_topics_intro(topics):
    print("checking topic [intro]")
    if topics['labels'][0] in ['self-introduction']:
        return "Good self-introduction. Thank you."

def greeting(cs, scoringModel, name):
    logging.info(f"Greeting start...")
    current_session = "greeting"

    greeting_lables = ["greeting", "confirm"] + repeat_labels
    intro_labels = ['self-introduction'] + repeat_labels

    # greeting
    greeting_text = f"Hello {name}. Nice to meet you. I am your interviewer."

    #create question
    greeting_q = Questionlv0(greeting_text)

    # greeting - response - recv answer (url of google cloud storage) from Android Client
    logging.info(f"waiting greeting - response - from clinet")
    greeting_chatbot_response,_,photoURL = scoringModel.giveResponse(greeting_q, current_session, greeting_lables, get_topics_greeting, photo_required = "true")
    
    print(f"duration_time: {greeting_q.duration_time}, response_time: {greeting_q.response_time}")
    photoURLs.append(photoURL)

    # greeting - response - from chatbot
    greeting_chatbot_response_url = google_tts(greeting_chatbot_response)
    cs.sendall(json_to_client(json_type_c, greeting_chatbot_response_url))
    print("send greeting - response - from chatbot (checked)")

    print("Sleep 5s for chatbot speak")
    time.sleep(5)

    # interviwee_intro
    interviwee_intro_text = "Please introduce yourself."

    # create question 
    interviwee_intro_q = Questionlv0(interviwee_intro_text)
    
    # interviwee_intro - response
    logging.info(f"waiting interviwee_intro - response - from clinet")
    
    intro_chatbot_response,_,photoURL = scoringModel.giveResponse(interviwee_intro_q, current_session, intro_labels, get_topics_intro, ignore_scores = True)

    intro_response_url = google_tts(intro_chatbot_response)
    print("interviwee_intro - response (checked) ->",intro_chatbot_response)

    cs.sendall(json_to_client(json_type_c, intro_response_url))
    logging.info("send greeting - response (checked)")

    print("Sleep 5s for chatbot speak")
    time.sleep(5)

    # add to questions dict for generate report
    questions_dict['greeting'] = [greeting_q, interviwee_intro_q]

    logging.info(f"Greeting end...")

def work_exp(scoringModel, job_exp_list, job_req):

    logging.info(f"Work_exp start...")

    current_session = "work_exp"
    
    job_req_with_labels = job_req + not_sure_labels + repeat_labels

    questions_text = ["In your working experience, as a {title} at {organization}. Can you talk about {responsibility} related to {topic}?",
                     "According to your working experience, being a {title} at {organization}. In the field of {topic}, can you talk about something related to {responsibility} ?"]

    questions_text_follow_up = ["Based on what you said, can you extend the content more in the field of {topic}?",
                            "I am interested in {topic}, can you talk some more based on what you just said?"]
    questionslv0 = []

    scoringModel.work_questions_response(job_exp_list, job_req, questions_text, questions_text_follow_up, questionslv0, current_session, job_req_with_labels)

    questions_dict['work_exp'] = questionslv0


def tech_skill(scoringModel):
    logging.info(f"tech_skill start...")
    current_session = "tech_skill"

    tech_skill_questions = []

    scoringModel.techskill_question_response(current_session, repeat_labels, tech_skill_questions)

    questions_dict['tech_skill'] = tech_skill_questions
    logging.info(f"tech_skill end...")

def bye(cs, scoringModel, name):
    logging.info(f"Bye start...")
    current_session = "bye"

    last_comment_text = "This is the last part of the interview. Do you have any comments to add?"
    # last_comment
    logging.info(f"send last_comment")

    # create question
    last_comment_text_q = Questionlv0(last_comment_text)

    photoURL = scoringModel.giveResponse_bye(last_comment_text_q, current_session, repeat_labels, photo_required = "true")

    photoURLs.append(photoURL)

    bye_text = f"{name}, Thank you for participating in this interview. It is the end of interview. You may turn off the application now. Good bye."
    print("bye_text ->", bye_text)
    bye_text_url = google_tts(bye_text)
    cs.sendall(json_to_client(json_type_f, bye_text_url))
    logging.info(f"send bye_text")

    questions_dict['bye'] = [last_comment_text_q]

def chatbot_start(cs, addr):
    logging.info(f"Client connected from {addr}")
    with cs:
        try:
            resume_id = '62036f5a00814558da9ff80b' # test
            # resume_id = '626fd427d149cdb9f17bd9f2' # Ho
            # resume_id = '626fedead149cdb9f17bd9f3' # TIN WAI MING
            # resume_id = '62700da3054b9d91f6e204ee' # HOI KING FAI
            # resume_id = '6271253602d55e08e061714a' # FD

            job_req = {'skills': ["PHP", "RESTful API", "O O P", "multithreading", "Spring Framework", "e-Payment", "nodejs", "web/mobile applications", "Jenkins", "SQL"] ,'edu': "degree", 'exp': "0 years"}
            # job_req = {'skills': ["SQL", "Python", "Data Scraping", "Machine Learning", "Database"] ,'edu': "degree", 'exp': "0 years"}
            
            report = gen_report(resume_id, job_req)
            logging.info(f"resume id: {resume_id}")

            f = open('QuestionTree.json')
            tech_question = json.load(f)

            scoringModel = ScoringModel(sessions,dont_understand_dict, repeat_dict, topics_asked, tech_question, cs)

            greeting(cs, scoringModel, report.interviewee.personalInfo.name)
            work_exp(scoringModel, report.interviewee.workExp.work_list, report.interviewee.job.skills)
            tech_skill(scoringModel)
            bye(cs, scoringModel,report.interviewee.personalInfo.name)
            print(questions_dict)
            report.questions_dict = questions_dict
            report_dict = report.toDict(dont_understand_dict, repeat_dict, topics_asked, photoURLs)
            print(report_dict)
            f = open("C:\\FYP\\output\\report.json", "w")
            json.dump(report_dict, f)
            f.close()
            report_db = insert_report(report_dict)
            print(report_db.inserted_id)
        except BaseException:
            logging.exception("Error !!!!!")

    logging.info(f"Client disconnected from {addr}")