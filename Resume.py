from Mongodb import get_collection, ObjectId

class Resume:
    def __init__(self, personalInfo, progLangs, softskills, langskills, workExp, education, cert):
        self.personalInfo = personalInfo
        self.progLangs = progLangs
        self.softskills = softskills
        self.langskills = langskills
        self.workExp = workExp
        self.education = education
        self.cert = cert

    def __repr__(self):
        return f"<Resume> \n{self.personalInfo}, \n{self.progLangs}, \n{self.softskills}, \n{self.langskills}, \n{self.workExp}, \n{self.education}, \n{self.cert}"

    def toDict(self):
        return {"personalInfo": self.personalInfo.toDict(), "progLangs": self.progLangs.toDict(), "softskills": self.softskills.toDict(), "langskills": self.langskills.toDict(), "workExp": self.workExp.toDict(), "education": self.education.toDict(), "cert": self.cert.toDict()}

class PersonalInfo:
    def __init__(self, name, phone, email, address):
        self.name = name
        self.phone = phone
        self.email = email
        self.address = address
    def __repr__(self):
        return f"<PersonalInfo> {self.name}, {self.phone}, {self.email}, {self.address}"
    def toDict(self):
        return {"name": self.name, "phone": self.phone, "email": self.email, "address": self.address}

class ProgLangs:
    def __init__(self, progLangs):
        self.progLangs = progLangs
    def __repr__(self):
        return f"<ProgLangs> {self.progLangs}"
    def toDict(self):
        return {"progLangs": self.progLangs}

class Softskills:
    def __init__(self, softskills):
        self.softskills = softskills
    def __repr__(self):
        return f"<Softskills> {self.softskills}"
    def toDict(self):
        return {"softskills": self.softskills}

class Langskills:
    def __init__(self, langskills):
        self.langskills = langskills
    def __repr__(self):
        return f"<Langskills> {self.langskills}"
    def toDict(self):
        return {"langskills": self.langskills}

class WorkExp:
    def __init__(self, work_list = []):
        self.work_list = work_list
    def __repr__(self):
        return f"<WorkExp> {self.work_list}"
    def toDict(self):
        return {"work_list": [w.toDict() for w in self.work_list]}

class Work:
    def __init__(self, title, organization, responsibility, startDate, endDate):
        self.title = title
        self.organization = organization
        self.responsibility = responsibility
        self.startDate = startDate
        self.endDate = endDate
    def __repr__(self):
        return f"<Work> {self.title}, {self.organization}, {self.responsibility}, {self.startDate}, {self.endDate}"
    def toDict(self):
        startDate = ""
        endDate = ""
        if self.startDate:
            startDate = self.startDate.strftime("%m/%d/%Y, %H:%M:%S")
        if self.endDate:
            endDate = self.endDate.strftime("%m/%d/%Y, %H:%M:%S")
        return {"title": self.title, "organization": self.organization, "title": self.responsibility, "startDate": startDate, "endDate": endDate}

class Educations:
    def __init__(self, edu_list = []):
        self.edu_list = edu_list
    def __repr__(self):
        return f"<Educations> {self.edu_list}"
    def toDict(self):
        return {"edu_list": [e.toDict() for e in self.edu_list]}

class Education:
    def __init__(self, title, level, organization, grade, startDate, endDate):
        self.title = title
        self.organization = organization
        self.level = level
        self.grade = grade
        self.startDate = startDate
        self.endDate = endDate
    def __repr__(self):
        return f"<Education> {self.title}, {self.organization}, {self.level}, {self.grade}, {self.startDate}, {self.endDate}"
    def toDict(self):
        startDate = ""
        endDate = ""
        if self.startDate:
            startDate = self.startDate.strftime("%m/%d/%Y, %H:%M:%S")
        if self.endDate:
            endDate = self.endDate.strftime("%m/%d/%Y, %H:%M:%S")
        return {"title": self.title, "organization": self.organization, "level": self.level, "grade": self.grade, "startDate": startDate, "endDate": endDate}

class Certification:
    def __init__(self, cert_list = []):
        self.cert_list = cert_list
    def __repr__(self):
        return f"<Certification> {self.cert_list}"
    def toDict(self):
        return {"cert_list": self.cert_list}

def gen_resume(id):
    collection = get_collection()
    item_details = collection.find_one({'_id': ObjectId(id)})
    # print(item_details)

    personalinfo = parseToPesonalInfo_fromdb(item_details)
    # skillset = parseToSkillset_fromdb(item_details)
    progLangs = parseToProgLangs_fromdb(item_details)
    softskills =parseToSoftskills_fromdb(item_details)
    langskills = parseToLangskills_fromdb(item_details)

    workExp = parseToWorkexp_fromdb(item_details)
    edu = parseToEdu_fromdb(item_details)
    cert = parseToCert_fromdb(item_details)

    return Resume(personalinfo, progLangs, softskills, langskills, workExp, edu, cert)

def parseToPesonalInfo_fromdb(item_details):
    name = item_details['Personal_Information']['name']
    phone = item_details['Personal_Information']['phone']
    email = item_details['Personal_Information']['email']
    address = item_details['Personal_Information']['address']
    return PersonalInfo(name, phone, email, address)

def parseToProgLangs_fromdb(item_details):
    progLangs = item_details['Programming_Language']
    return ProgLangs(progLangs)

def parseToSoftskills_fromdb(item_details):
    softskills = item_details['Soft_Skills']
    return Softskills(softskills)

def parseToLangskills_fromdb(item_details):
    langskills = item_details['Language_Skills']
    return Langskills(langskills)

def parseToWorkexp_fromdb(item_details):
    Work_Experience = item_details['Work_Experience']
    work_list = []
    for workexp in Work_Experience:
        title = workexp['job_title']
        organization = workexp['organization']
        startDate = workexp['startDate']
        endDate = workexp['endDate']
        responsibility = workexp['responsibility']

        work_list.append(Work(title, organization, responsibility, startDate, endDate))
    return WorkExp(work_list)

def parseToEdu_fromdb(item_details):
    education = item_details['Education']
    edu_list = []
    for edu in education:
        title = edu['title']
        level = edu['education_level']
        organization = edu['organization']
        grade = edu['grade']
        startDate = edu['startDate']
        endDate = edu['completionDate']
        
        edu_list.append(Education(title, level, organization, grade, startDate, endDate))
    return Educations(edu_list)

def parseToCert_fromdb(item_details):
    cert = item_details['Certification']
    return Certification(cert)

if __name__ == "__main__":
    # print(gen_resume('62036b0500814558da9ff80a'))
    report = gen_resume('62036f5a00814558da9ff80b')
    print(report.toDict())
    
