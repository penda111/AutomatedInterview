class Question(object):
    
    def __init__(self, level, question_text):
        self.level = level
        self.question_text = question_text
        self.question_ans = None
        self.question_ans_summary = None
        self.duration_time = None
        self.response_time = None
        self.question_ans_keywords = []
        self.question_ans_summary_keywords = []
        self.topic = None

        # work_exp only
        self.job_title = None
        self.job_organization = None
        self.job_responsibility = None

class Questionlv0(Question):

    def __init__(self, question_text):
        super().__init__(level = 0, question_text = question_text)
        self.max_follow_up_q = 2
        self.follow_up_qs = []

    def check_available_follow_up_q(self):
        if len(self.follow_up_qs) == self.max_follow_up_q:
            return False
        return True

    def add_follow_up_q(self, question):
        if self.check_available_follow_up_q():
            if isinstance(question, str):
                self.follow_up_qs.append(Questionlv1(question))
            elif isinstance(question, Question):
                qlv1 = Questionlv1(question.question_text)
                qlv1.topic = question.topic
                self.follow_up_qs.append(qlv1)

    def __repr__(self):
        return f"<Questionlv0> \n{self.question_text}, \n{self.question_ans}, \n{self.follow_up_qs}, \n D:{self.duration_time}, \n R:{self.response_time}"

    def toDict(self):
        if self.job_title:
            return {"Questionlv":0, "question_text": self.question_text, "question_ans": self.question_ans, "question_ans_summary": self.question_ans_summary, "duration_time": self.duration_time, "response_time": self.response_time, "follow_up_qs": [f.toDict() for f in self.follow_up_qs], "job_title": self.job_title, "job_organization": self.job_organization, "job_responsibility": self.job_responsibility, "keywords": self.question_ans_keywords, "summary_keywords": self.question_ans_summary_keywords, "topic": self.topic}
        else:
            return {"Questionlv":0, "question_text": self.question_text, "question_ans": self.question_ans, "question_ans_summary": self.question_ans_summary, "duration_time": self.duration_time, "response_time": self.response_time, "follow_up_qs": [f.toDict() for f in self.follow_up_qs], "keywords": self.question_ans_keywords, "summary_keywords": self.question_ans_summary_keywords, "topic": self.topic}

class Questionlv1(Question):

    def __init__(self, question_text):
        super().__init__(level = 1, question_text = question_text)
        self.max_follow_up_q = 2
        self.follow_up_qs = []

    def check_available_follow_up_q(self):
        if len(self.follow_up_qs) == self.max_follow_up_q:
            return False
        return True

    def add_follow_up_q(self, question):
        if self.check_available_follow_up_q():
            if isinstance(question, str):
                self.follow_up_qs.append(Questionlv2(question))
            elif isinstance(question, Question):
                qlv2 = Questionlv2(question.question_text)
                qlv2.topic = question.topic
                self.follow_up_qs.append(qlv2)
    
    def __repr__(self):
        return f"<Questionlv1> \n{self.question_text}, \n{self.question_ans}, \n{self.follow_up_qs}, \n D:{self.duration_time}, \n R:{self.response_time}"

    def toDict(self):
        return {"Questionlv":1, "question_text": self.question_text, "question_ans": self.question_ans, "question_ans_summary": self.question_ans_summary, "duration_time": self.duration_time, "response_time": self.response_time, "follow_up_qs": [f.toDict() for f in self.follow_up_qs], "keywords": self.question_ans_keywords, "summary_keywords": self.question_ans_summary_keywords, "topic": self.topic}

class Questionlv2(Question):
    def __init__(self, question_text):
        super().__init__(level = 2, question_text = question_text)

    def __repr__(self):
        return f"<Questionlv2> \n{self.question_text}, \n{self.question_ans}, \n D:{self.duration_time}, \n R:{self.response_time}"

    def toDict(self):
        return {"Questionlv":2, "question_text": self.question_text, "question_ans": self.question_ans, "question_ans_summary": self.question_ans_summary, "duration_time": self.duration_time, "response_time": self.response_time, "keywords": self.question_ans_keywords, "summary_keywords": self.question_ans_summary_keywords, "topic": self.topic}


if __name__ == "__main__":

    obj2 = Question(-1, "According to your working experience, being a Web Developer at Luna Web Design. In the field of web/mobile applications, can you talk about how Develop project concepts and maintain optimal workflow is related?")

    obj2new = Questionlv1(obj2.question_text)

    print(obj2new.toDict())