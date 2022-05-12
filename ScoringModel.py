from Google_cloud_api import google_tts, google_stt
from Nlp_function import classifier 
import logging, random, json, time
from Question import Question, Questionlv0

json_type_q = "question"
json_type_c = "comment"
json_type_f = "finish"
repeat_labels = ["ask me to repeat"]
not_sure_labels = ["not sure"]
dont_understand_text = "I'm not sure what you're talking about. Can you repeat it in a simpler way?"
repeat_text = "repeat"

class ScoringModel(object):
    
    def __init__(self, session_dict, dont_understand_dict, repeat_dict, topics_asked, technical_questions, cs):
        self.session_dict = session_dict
        self.dont_understand_dict = dont_understand_dict
        self.repeat_dict = repeat_dict
        self.topics_asked = topics_asked
        self.technical_questions = technical_questions
        self.technical_questions_counter = technical_questions.fromkeys(technical_questions, 0)
        self.cs = cs

    def json_to_client(self, type, url, photo_required = "false"):
        return bytes(json.dumps({"type":type ,"url":url, "photo": photo_required}),encoding="utf-8")

    def get_related_topics(self, sequence_to_classify, candidate_labels):
        result = classifier(sequence_to_classify, candidate_labels, multi_label = True)
        # print(result)
        return result

    def get_topics_scores(self, sequences, candidate_labels):
        results = []
        if isinstance(sequences, list):
            for sequence_to_classify in sequences:
                results.append(self.get_related_topics(sequence_to_classify, candidate_labels))
        elif isinstance(sequences, str):
            results.append(self.get_related_topics(sequences, candidate_labels))
        potential_topics = []
        for r in results:
            temp = {'sequence': r['sequence'], 'labels': r['labels'], 'scores': r['scores']}
            potential_topics.append(temp)
        return potential_topics

    def check_dk_repeat(self, topics, ignore_scores):
        print("cheking dk repeat ....")
        if topics['scores'][0] < 0.4:
            if ignore_scores:
                return ""
            return dont_understand_text
        if (any(x in topics['labels'][0:2] for x in repeat_labels) and any(x in topics['labels'][0:2] for x in not_sure_labels)):
            if topics['scores'][1] > 0.7:
                return repeat_text
            return ""
        elif any(x in topics['labels'][0] for x in repeat_labels):
            if topics['labels'][0] in repeat_labels:
                return repeat_text
            return ""
        return ""

    def giveResponse(self, question, session, labels, get_topics, photo_required = "false", ignore_scores = False):
        # ask 
        question_text_url = google_tts(question.question_text)
        self.cs.sendall(self.json_to_client(json_type_q, question_text_url, photo_required))
        print("question_text -> ", question.question_text)

        # receive
        response = ""
        data = self.cs.recv(1024).decode('utf-8')
        parsed_data = json.loads(data)
        text_stt = google_stt(parsed_data['url'])
        print("response - from clinet (stt) ->",text_stt)

        # get topics
        topics = self.get_topics_scores(text_stt, labels)
        print("topics ->", topics)
        # check if response is "dont_understand" or "repeat"
        response = self.check_dk_repeat(topics[0], ignore_scores)
        print("check_dk_repeat ->", response)

        # If response is "dont_understand" or "repeat"
        while response == dont_understand_text or response == repeat_text:
            if response == dont_understand_text:
                self.dont_understand_dict[session] += 1

                response_url = google_tts(response)
                self.cs.sendall(self.json_to_client(json_type_q, response_url))
                logging.info(f"send dont_know")

                logging.info(f"waiting dont_know - response")
                data = self.cs.recv(1024).decode('utf-8')
                parsed_data = json.loads(data)
                text_stt = google_stt(parsed_data['url'])
                print("text_stt from dont_know=>",text_stt)
                topics = self.get_topics_scores(text_stt, labels)
                print("topics ->", topics)
                response = self.check_dk_repeat(topics[0], ignore_scores)
                # response = get_topics(text_stt, labels)
                print("response from dont_know=>",response)

            elif response == repeat_text:
                self.repeat_dict[session] += 1
                self.cs.sendall(self.json_to_client(json_type_q, question_text_url, photo_required))
                logging.info(f"re_sending (repeat question)")

                logging.info(f"waiting Re_sending - response")
                data = self.cs.recv(1024).decode('utf-8')
                parsed_data = json.loads(data)
                text_stt = google_stt(parsed_data['url'])
                print("text_stt from re_sending=>",text_stt)
                topics = self.get_topics_scores(text_stt, labels)
                print("topics ->", topics)
                response = self.check_dk_repeat(topics[0], ignore_scores)
                # response = get_topics(text_stt, labels)
                print("response from re_sending=>",response)

        # getting response
        response = get_topics(topics[0])
        photoURL = parsed_data['photoURL']

        question.question_ans = text_stt
        question.duration_time = parsed_data['duration_time']
        question.response_time = parsed_data['response_time']
        question.topic = session

        return response, topics, photoURL

    def giveResponse_techskill(self, question, session, labels, ignore_scores = True):
        # ask 
        question_text_url = google_tts(question.question_text)
        self.cs.sendall(self.json_to_client(json_type_q, question_text_url))
        print("question_text -> ", question.question_text)

        # receive
        response = ""
        data = self.cs.recv(1024).decode('utf-8')
        parsed_data = json.loads(data)
        text_stt = google_stt(parsed_data['url'])
        print("response - from clinet (stt) ->",text_stt)

        # get topics
        topics = self.get_topics_scores(text_stt, labels)
        print("topics ->", topics)
        # check if response is "dont_understand" or "repeat"
        response = self.check_dk_repeat(topics[0], ignore_scores)
        print("check_dk_repeat ->", response)

        # If response is "dont_understand" or "repeat"
        while response == dont_understand_text or response == repeat_text:
            if response == dont_understand_text:
                self.dont_understand_dict[session] += 1

                response_url = google_tts(response)
                self.cs.sendall(self.json_to_client(json_type_q, response_url))
                logging.info(f"send dont_know")

                logging.info(f"waiting dont_know - response")
                data = self.cs.recv(1024).decode('utf-8')
                parsed_data = json.loads(data)
                text_stt = google_stt(parsed_data['url'])
                print("text_stt from dont_know=>",text_stt)
                topics = self.get_topics_scores(text_stt, labels)
                print("topics ->", topics)
                response = self.check_dk_repeat(topics[0], ignore_scores)
                # response = get_topics(text_stt, labels)
                print("response from dont_know=>",response)

            elif response == repeat_text:
                self.repeat_dict[session] += 1
                self.cs.sendall(self.json_to_client(json_type_q, question_text_url))
                logging.info(f"re_sending (repeat question)")

                logging.info(f"waiting Re_sending - response")
                data = self.cs.recv(1024).decode('utf-8')
                parsed_data = json.loads(data)
                text_stt = google_stt(parsed_data['url'])
                print("text_stt from re_sending=>",text_stt)
                topics = self.get_topics_scores(text_stt, labels)
                print("topics ->", topics)
                response = self.check_dk_repeat(topics[0], ignore_scores)
                # response = get_topics(text_stt, labels)
                print("response from re_sending=>",response)

        question.question_ans = text_stt
        question.duration_time = parsed_data['duration_time']
        question.response_time = parsed_data['response_time']
        question.topic = session

    def giveResponse_bye(self, question, session, labels, photo_required = "true", ignore_scores = True):
        # ask 
        question_text_url = google_tts(question.question_text)
        self.cs.sendall(self.json_to_client(json_type_q, question_text_url, photo_required))
        print("question_text -> ", question.question_text)

        # receive
        response = ""
        data = self.cs.recv(1024).decode('utf-8')
        parsed_data = json.loads(data)
        text_stt = google_stt(parsed_data['url'])
        print("response - from clinet (stt) ->",text_stt)

        # get topics
        topics = self.get_topics_scores(text_stt, labels)
        print("topics ->", topics)
        # check if response is "dont_understand" or "repeat"
        response = self.check_dk_repeat(topics[0], ignore_scores)
        print("check_dk_repeat ->", response)

        # If response is "dont_understand" or "repeat"
        while response == dont_understand_text or response == repeat_text:
            if response == dont_understand_text:
                self.dont_understand_dict[session] += 1

                response_url = google_tts(response)
                self.cs.sendall(self.json_to_client(json_type_q, response_url))
                logging.info(f"send dont_know")

                logging.info(f"waiting dont_know - response")
                data = self.cs.recv(1024).decode('utf-8')
                parsed_data = json.loads(data)
                text_stt = google_stt(parsed_data['url'])
                print("text_stt from dont_know=>",text_stt)
                topics = self.get_topics_scores(text_stt, labels)
                print("topics ->", topics)
                response = self.check_dk_repeat(topics[0], ignore_scores)
                # response = get_topics(text_stt, labels)
                print("response from dont_know=>",response)

            elif response == repeat_text:
                self.repeat_dict[session] += 1
                self.cs.sendall(self.json_to_client(json_type_q, question_text_url, photo_required))
                logging.info(f"re_sending (repeat question)")

                logging.info(f"waiting Re_sending - response")
                data = self.cs.recv(1024).decode('utf-8')
                parsed_data = json.loads(data)
                text_stt = google_stt(parsed_data['url'])
                print("text_stt from re_sending=>",text_stt)
                topics = self.get_topics_scores(text_stt, labels)
                print("topics ->", topics)
                response = self.check_dk_repeat(topics[0], ignore_scores)
                # response = get_topics(text_stt, labels)
                print("response from re_sending=>",response)

        photoURL = parsed_data['photoURL']

        question.question_ans = text_stt
        question.duration_time = parsed_data['duration_time']
        question.response_time = parsed_data['response_time']
        question.topic = session

        return photoURL

    def techskill_question_response(self, session, labels, tech_skill_questions):
        count = 0
        for tech_topic in self.technical_questions.keys():
            # ask two question for demo only (shorten interview time)
            if count >= 2:
                return
            print(self.topics_asked)
            if self.topics_asked[tech_topic] == 0:
                question_text = random.choice(self.technical_questions[tech_topic]["easy"])
                question =  Questionlv0(question_text)

                self.giveResponse_techskill(question, session, labels)
                tech_skill_questions.append(question)

                thankyou_text = "Thanks for your answer."
                thankyou_text_url = google_tts(thankyou_text)
                self.cs.sendall(self.json_to_client(json_type_c, thankyou_text_url))
                logging.info(f"send thankyou_text")

                # sleep
                time.sleep(4)

                question_text = random.choice(self.technical_questions[tech_topic]["difficult"])
                question =  Questionlv0(question_text)

                self.giveResponse_techskill(question, session, labels)
                tech_skill_questions.append(question)

                thankyou_text = "Thanks for your answer."
                thankyou_text_url = google_tts(thankyou_text)
                self.cs.sendall(self.json_to_client(json_type_c, thankyou_text_url))
                logging.info(f"send thankyou_text")

                self.topics_asked[tech_topic] += 1
                # sleep
                time.sleep(4)
                count += 1

            elif self.topics_asked[tech_topic] == 1:
                question_text = random.choice(self.technical_questions[tech_topic]["difficult"])
                question =  Questionlv0(question_text)

                self.giveResponse_techskill(question, session, labels)
                tech_skill_questions.append(question)

                thankyou_text = "Thanks for your answer."
                thankyou_text_url = google_tts(thankyou_text)
                self.cs.sendall(self.json_to_client(json_type_c, thankyou_text_url))
                logging.info(f"send thankyou_text")

                self.topics_asked[tech_topic] += 1
                # sleep
                time.sleep(4)
                count += 1

            

    def giveResponse_work(self, question, session, labels, photo_required = "false", ignore_scores = False):
        # ask 
        question_text_url = google_tts(question.question_text)
        self.cs.sendall(self.json_to_client(json_type_q, question_text_url))
        print("question_text -> ", question.question_text)

        # receive
        response = ""
        data = self.cs.recv(1024).decode('utf-8')
        parsed_data = json.loads(data)
        text_stt = google_stt(parsed_data['url'])
        print("response - from clinet (stt) ->",text_stt)

        # get topics
        topics = self.get_topics_scores(text_stt, labels)
        print("topics ->", topics)
        # check if response is "dont_understand" or "repeat"
        response = self.check_dk_repeat(topics[0], ignore_scores)
        print("check_dk_repeat ->", response)

        while response == dont_understand_text or response == repeat_text:
            if response == dont_understand_text:
                self.dont_understand_dict[session] += 1

                response_url = google_tts(response)
                self.cs.sendall(self.json_to_client(json_type_q, response_url))
                logging.info(f"send dont_know")

                logging.info(f"waiting dont_know - response")
                data = self.cs.recv(1024).decode('utf-8')
                parsed_data = json.loads(data)
                text_stt = google_stt(parsed_data['url'])
                print("text_stt from dont_know=>",text_stt)
                topics = self.get_topics_scores(text_stt, labels)
                print("topics ->", topics)
                response = self.check_dk_repeat(topics[0], ignore_scores)
                # response = get_topics(text_stt, labels)
                print("response from dont_know=>",response)

            elif response == repeat_text:
                self.repeat_dict[session] += 1
                self.cs.sendall(self.json_to_client(json_type_q, question_text_url, photo_required))
                logging.info(f"re_sending (repeat question)")

                logging.info(f"waiting Re_sending - response")
                data = self.cs.recv(1024).decode('utf-8')
                parsed_data = json.loads(data)
                text_stt = google_stt(parsed_data['url'])
                print("text_stt from re_sending=>",text_stt)
                topics = self.get_topics_scores(text_stt, labels)
                print("topics ->", topics)
                response = self.check_dk_repeat(topics[0], ignore_scores)
                # response = get_topics(text_stt, labels)
                print("response from re_sending=>",response)

        question.question_ans = text_stt
        question.duration_time = parsed_data['duration_time']
        question.response_time = parsed_data['response_time']
        question.topic = session

        return topics

    def follow_up_question(self, questionslv0, questions_text_follow_up ,session, labels):
        for lv0 in questionslv0:
            questionslv1 = []

            # ask question (lv0)
            logging.info(f"send question (lv0)")
            potential_topics = self.giveResponse_work(lv0, session, labels)

            self.create_question_followup_workexp(potential_topics, questions_text_follow_up, questionslv1)

            # add follow up question to question lv0
            [lv0.add_follow_up_q(qulv1) for qulv1 in questionslv1]
            # print(lv0)

            # ask follow up question (lv1)
            for lv1 in lv0.follow_up_qs:
                questionslv2 = []

                # ask question (lv1)
                logging.info(f"send question (lv1)")
                potential_topics = self.giveResponse_work(lv1, session, labels)

                self.create_question_followup_workexp(potential_topics, questions_text_follow_up, questionslv2)

                # add follow up question to question lv1
                [lv1.add_follow_up_q(qulv2) for qulv2 in questionslv2]

                # ask follow up question (lv2)
                for lv2 in lv1.follow_up_qs:
                    # ask question (lv2)

                    potential_topics = self.giveResponse_work(lv2, session, labels)

    def work_questions_response(self, job_exp_list, job_req, questions_text, questions_text_follow_up, questionslv0, session, labels):
        for work_exp in job_exp_list[0:2]:  # For demo only ask 2 job
            job_responsibilitys = work_exp.responsibility 
            job_title = work_exp.title
            job_organization = work_exp.organization
            potential_topics = self.get_topics_scores(job_responsibilitys[0:1], job_req) # For demo only ask 1 question
            self.create_questionlv0_workexp(potential_topics, questions_text, questionslv0, job_title, job_organization)
        
        self.follow_up_question(questionslv0, questions_text_follow_up, session, labels)
        
    def create_questionlv0_workexp(self, potential_topics, questions_text, questions, job_title, job_organization):
        for topics in potential_topics:
            length_of_labels = len(topics['labels'])
            for i in range(0, length_of_labels):
                if self.topics_asked[topics['labels'][i]] < 2:
                    question = Questionlv0(random.choice(questions_text).format(title=job_title, organization = job_organization, responsibility= topics['sequence'], topic= topics['labels'][i]))
                    question.job_title = job_title
                    question.job_organization = job_organization
                    question.job_responsibility = topics['sequence']
                    question.topic = topics['labels'][i]

                    questions.append(question)
                    self.topics_asked[topics['labels'][i]] += 1
                    break

    def create_question_followup_workexp(self, potential_topics, questions_text, questions):
        print("[create_question_followup_workexp]")
        for topics in potential_topics:
            # print(topics)
            length_of_labels = len(topics['labels'])
            for i in range(0, length_of_labels):
                # if the label is not sure
                if topics['labels'][i] in not_sure_labels:
                    print('Interviewee Not sure')
                    break
                # if the label is repeat
                elif topics['labels'][i] in repeat_labels:
                    print('Ask for repeat') 
                    break
                # if the confidence is less than 0.4
                elif topics['scores'][i] < 0.4:
                    print("Chatbot doesn't understand topic")
                    break
                elif self.topics_asked[topics['labels'][i]] < 2:
                    question = Question(-1, random.choice(questions_text).format(topic= topics['labels'][i]))
                    question.topic = topics['labels'][i]
                    questions.append(question)
                    self.topics_asked[topics['labels'][i]] += 1
                    break