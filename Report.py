from Resume import gen_resume
from Interviewee import Interviewee
from Nlp_function import summarizer, nlp

class Report:
    def __init__(self, interviewee, questions_dict = None):
        self.interviewee = interviewee
        self.questions_dict = questions_dict

    def __repr__(self):
        return f"<Report> \n{self.interviewee}, \n{self.questions_dict}"

    def toDict(self, num_dont_understand, num_repeat, asked_topics, photoURLs):

        total_response_time = 0
        total_duration_time = 0
        summarizer_rate = 0.5
        summarizer_words_limit = 60

        for session in self.questions_dict:
            for q in self.questions_dict[session]:
                print(type(q.response_time))
                total_response_time += int(q.response_time)
                total_duration_time += int(q.duration_time)
                num_text = len(q.question_ans.split())
                print(num_text)
                if num_text > summarizer_words_limit/summarizer_rate:
                    print(f">{summarizer_words_limit}")
                    q.question_ans_summary = summarizer(q.question_ans, max_length=int(num_text * summarizer_rate), min_length=int(summarizer_words_limit * summarizer_rate), do_sample=False)[0]['summary_text']
                else:
                    print(f"<{summarizer_words_limit}")
                q.question_ans_keywords = [ent.text for ent in list(nlp(q.question_ans).ents)]
                if q.question_ans_summary:
                    q.question_ans_summary_keywords = [ent.text for ent in list(nlp(q.question_ans_summary).ents)]
                for qlv1 in q.follow_up_qs:
                    total_response_time += int(qlv1.response_time)
                    total_duration_time += int(qlv1.duration_time)
                    num_text = len(qlv1.question_ans.split())
                    if num_text > summarizer_words_limit/summarizer_rate:
                        qlv1.question_ans_summary = summarizer(qlv1.question_ans, max_length=int(num_text * summarizer_rate), min_length=int(summarizer_words_limit * summarizer_rate), do_sample=False)[0]['summary_text']
                    qlv1.question_ans_keywords = [ent.text for ent in list(nlp(qlv1.question_ans).ents)]
                    if qlv1.question_ans_summary:
                        qlv1.question_ans_summary_keywords = [ent.text for ent in list(nlp(qlv1.question_ans_summary).ents)]
                    for qlv2 in qlv1.follow_up_qs:
                        total_response_time += int(qlv2.response_time)
                        total_duration_time += int(qlv2.duration_time)
                        num_text = len(qlv2.question_ans.split())
                        if num_text > summarizer_words_limit/summarizer_rate:
                            qlv2.question_ans_summary = summarizer(qlv2.question_ans, max_length=int(num_text * summarizer_rate), min_length=int(summarizer_words_limit * summarizer_rate), do_sample=False)[0]['summary_text']
                        qlv2.question_ans_keywords = [ent.text for ent in list(nlp(qlv2.question_ans).ents)]
                        if qlv2.question_ans_summary:
                            qlv2.question_ans_summary_keywords = [ent.text for ent in list(nlp(qlv2.question_ans_summary).ents)]

        return {"interviewee": self.interviewee.toDict(), "questions_dict": { session:[q.toDict() for q in self.questions_dict[session]] for session in self.questions_dict}, "total_response_time": total_response_time, "total_duration_time": total_duration_time, "num_dont_understand": num_dont_understand, "num_repeat":num_repeat, "asked_topics": asked_topics, "photoURLs": photoURLs}

def gen_report(id, job_req):
    resume = gen_resume(id)
    interviewee = Interviewee(resume,job_req)
    report = Report(interviewee)
    # print(report)
    return report


