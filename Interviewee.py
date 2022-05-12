from Resume import gen_resume

class Interviewee:
    def __init__(self, resume, job_req):
        self.resume = resume
        self.personalInfo = resume.personalInfo
        self.progLangs = resume.progLangs
        self.softskills = resume.softskills
        self.langskills = resume.langskills
        self.workExp = resume.workExp
        self.education = resume.education
        self.cert = resume.cert
        self.projects = Projects()
        self.job = Job(job_req)  

    def __repr__(self):
        return f"<Interviewee> \n{self.resume}, \n{self.projects}, \n{self.job}"

    def toDict(self):
        return {"personalInfo": self.personalInfo.toDict(), "progLangs": self.progLangs.toDict(), "softskills": self.softskills.toDict(), "langskills": self.langskills.toDict(), "workExp": self.workExp.toDict(), "education": self.education.toDict(), "cert": self.cert.toDict(), "job_req": self.job.toDict()}

        # return {"resume": self.resume.toDict(), "personalInfo": self.personalInfo.toDict(), "progLangs": self.progLangs.toDict(), "softskills": self.softskills.toDict(), "langskills": self.langskills.toDict(), "workExp": self.workExp.toDict(), "education": self.education.toDict(), "cert": self.cert.toDict()}

class Projects:
    def __init__(self, project_list=[]):
        self.project_list = project_list
    def __repr__(self):
        return f"<Projects> {self.project_list}"

class Project:
    aim = None
    overcome = None
    learnt = None
    tech_used = None

    def __repr__(self):
        return f"<Project> {self.title}, {self.aim}, {self.overcome}, {self.learnt}, {self.tech_used}"

class Job:
    def __init__(self, job_req):
        self.skills = job_req['skills']
        self.edu = job_req['edu']
        self.exp = job_req['exp']
    def __repr__(self):
        return f"<Job> {self.skills}, {self.edu}, {self.exp}"

    def toDict(self):
        return {"skills":self.skills, "edu": self.edu, "exp":self.exp}

if __name__ == "__main__":
    resume = gen_resume('62036f5a00814558da9ff80b')
    job_req = {'skills': ["PHP", "RESTful API", "AWS Cloud Service", "Docker", "e-Payment", "e-Commerce", "web/mobile applications", "Jenkins", "MSSQL"] ,'edu': "degree", 'exp': "3 years"}
    # print(job_req['skills'])
    interviewee = Interviewee(resume,job_req)
    print(interviewee.toDict())
    pass

