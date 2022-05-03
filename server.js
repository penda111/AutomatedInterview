const express = require('express'); 
const path = require("path");
const app = express();
const multer = require("multer")
const {AffindaCredential, AffindaAPI} = require("@affinda/affinda");
const MongoClient = require('mongodb').MongoClient;
const ObjectID = require('mongodb').ObjectID;
const assert = require('assert');
const http = (require('http'));
const fs = (require('fs'));


const mongourl ='';
const dbName='';

// View Engine Setup
app.set("views",path.join(__dirname,"views"))
app.set("view engine","ejs")

app.get("/ResumeUpload",function(req,res){
    res.render("ResumeUpload");
})

// var upload = multer({ dest: "Upload_folder_name" })
// If you do not want to use diskStorage then uncomment it
var storage = multer.diskStorage({
    destination: function (req, file, cb) {
  
        // Uploads is the Upload_folder_name
        cb(null, "uploads")
    },
    filename: function (req, file, cb) {
      cb(null, Resumefilename)
    }
  })

const maxSize = 1 * 10000 * 10000;
var Resumefilename;
var upload = multer({ 
    storage: storage,
    limits: { fileSize: maxSize },
    fileFilter: function (req, file, cb){
    
        // Set the filetypes, it is optional
        var filetypes = /pdf/;
        var mimetype = filetypes.test(file.mimetype);
        var extname = filetypes.test(path.extname(
                    file.originalname).toLowerCase());
        
        if (mimetype && extname) {
			Resumefilename = file.originalname.split('.')[0]+"-"+Date.now()+".pdf";
            return cb(null, true);
        }
      
        cb("Error: File upload only supports the "
                + "following filetypes - " + filetypes);
      } 
  
// resume is the name of file attribute
}).single("resume");    

app.post("/uploadResume",function (req, res, next) {
        
    // Error MiddleWare for multer file upload, so if any
    // error occurs, the resume would not be uploaded!
    upload(req,res,function(err) {  
        if(err) { 
            // ERROR occured (here it can be occured due
            // to uploading resume of size greater than
            // 1MB or uploading different file type)
            res.send(err)
        }
        else {
            // SUCCESS, resume successfully uploaded
			const credential = new AffindaCredential("");
			const client = new AffindaAPI(credential);
			const filepath =  "uploads/"+Resumefilename;
			const readStream = fs.createReadStream(filepath);
			client.createResume({file: readStream}).then((result) => {
				client.deleteResume(result.meta.identifier);
				handle_insert(result,function(callback){
					 if(callback){
						  res.status(200).send("Upload Success! id:"+callback);
					 }
					 else res.status(404).send("Upload Fail");
				});
			}).catch((err) => {
				console.log("An error occurred:");
				console.error(err);
			});	
			fs.unlink(filepath, function (err) {
				if (err) throw err;
				// if no error, file has been deleted successfully
				console.log('File deleted!');
			});
        }
    })
})

function handle_insert(data,callback){
	var resume = {};
	resume['Personal_Information']={};
	resume['Personal_Information'].name = data.data.name.raw;
	resume['Personal_Information'].phone = data.data.phoneNumbers[0];
	resume['Personal_Information'].email = data.data.emails[0];
	//resume['Personal_Information'].address = data.data.location.rawInput;
	resume['Personal_Information'].address = "Hong Kong";

	resume['Programming_Language'] = [];
	resume['Soft_Skills'] = [];
	resume['Language_Skills'] = [];
	resume['Work_Experience'] = [];
	resume['Education'] = [];
	resume['Certification'] = [];

	for(var i=0;i< data.data.skills.length; i++){
		if(data.data.skills[i].type == "hard_skill"){
			var  programming_Language;
			programming_Language = data.data.skills[i].name;
			if(programming_Language.includes("(Programming Language)")){
				programming_Language = programming_Language.replace("(Programming Language)",'').trim();
			}
			if(programming_Language.includes("((Scripting Language))")){
				programming_Language =  programming_Language.replace("((Scripting Language))",'').trim();
			}		
			resume['Programming_Language'].push(programming_Language);
		}
	}
	

	for(var i=0;i< data.data.skills.length; i++){
		if(data.data.skills[i].type == "soft_skill")
			resume['Soft_Skills'].push(data.data.skills[i].name);
	}
	for(var i=0;i< data.data.languages.length; i++){
		var language = {}
		language['title'] = data.data.languages[i];
		resume['Language_Skills'].push(language);
	}
	for(var i=0;i< data.data.education.length; i++){
		var education = {}
		education['title'] = data.data.education[i].accreditation.education;
		education['education_level'] = data.data.education[i].accreditation.educationLevel;
		education['organization'] = data.data.education[i].organization;
		education['grade'] = data.data.education[i].grade;
		education['startDate'] = data.data.education[i].dates.startDate;
		education['completionDate'] = data.data.education[i].dates.completionDate;
		resume['Education'].push(education);
	}

	for(var i=0;i< data.data.workExperience.length; i++){
		var work_Experience = {}
		work_Experience['job_title'] = data.data.workExperience[i].jobTitle;
		work_Experience['organization'] = data.data.workExperience[i].organization;
		work_Experience['startDate'] = data.data.workExperience[i].dates.startDate;
		work_Experience['endDate'] = data.data.workExperience[i].dates.endDate;
		work_Experience['responsibility'] = data.data.workExperience[i].jobDescription.replace("Responsibilities:",'').trim().split("\n");
		for(var n=0; n<work_Experience['responsibility'].length;n++){
			work_Experience['responsibility'][n] = work_Experience['responsibility'][n].replace(/[^\w\s]/gi, '').trim();
		}
		resume['Work_Experience'].push(work_Experience);
	}
	for(var i=0;i< data.data.certifications.length; i++){
		resume['Certification'].push(data.data.certifications[i]);
	}

	
    insertResume(resume, (results) => {
        if (results.acknowledged) {
			console.log("insert resume success");
			var interviewee = resume;
			interviewee['project'] = [];
			interviewee['job'] = {};
			interviewee['job'].requireExp = "";
			interviewee['job'].requireEdu = "";
			interviewee['job'].requireSkill = [];
				
			insertInterviewee(interviewee, (results) => {
				if (results.acknowledged) {
					console.log(results);
					console.log("insert interviewee success");
					callback(results.insertedId);
				} else {
					console.log("insert interviewee fail");
					callback();
				}		
			});									
        } else {
			console.log("insert resume fail");
        }		
    });	
}

const insertResume = (doc,callback) => {
  const client = new MongoClient(mongourl);
    client.connect((err) => {
        assert.equal(null, err);
        console.log("Connected successfully to server");
        const db = client.db(dbName);
    db.collection('resume').insertOne(doc, (err, results) => {
    assert.equal(err,null);
    console.log("inserted one document " + JSON.stringify(doc));
    callback(results);
   });
  });
}

const insertInterviewee = (doc,callback) => {
  const client = new MongoClient(mongourl);
    client.connect((err) => {
        assert.equal(null, err);
        console.log("Connected successfully to server");
        const db = client.db(dbName);
    db.collection('interviewee').insertOne(doc, (err, results) => {
    assert.equal(err,null);
    console.log("inserted one document " + JSON.stringify(doc));
    callback(results);
   });
  });
}


const findDocument = (db, criteria, callback) => {
    let cursor = db.collection('report').find(criteria);
    console.log(`findDocument: ${JSON.stringify(criteria)}`);
    cursor.toArray((err,docs) => {
        assert.equal(err,null);
        console.log(`findDocument: ${docs.length}`);
        callback(docs);
    });
}

const handle_Find = (req,res, criteria) => {
    const client = new MongoClient(mongourl);
    client.connect((err) => {
        assert.equal(null, err);
        console.log("Connected successfully to server");
        const db = client.db(dbName);
		var test;
		var checkForHexRegExp = /^(?=[a-f\d]{24}$)(\d+[a-f]|[a-f]+\d)/i;
		console.log("test:"+criteria);
		if(checkForHexRegExp.test(criteria)){
			test = ObjectID(criteria); // hard-code
			console.log("true");
		}
		else test = ObjectID("621b720f16fb9f02779a8d1e");
        findDocument(db, test, (docs) => {
            client.close();
            res.status(200).render('report',{docs:docs});
        });
    });
}

const handle_FindAll = (req, res, criteria) => {
	const client = new MongoClient(mongourl);
    client.connect((err) => {
        assert.equal(null, err);
        console.log("Connected successfully to server");
        const db = client.db(dbName);

		console.log("handle_FindAll:"+criteria);
        findDocument(db, criteria, (docs) => {
            client.close();
            res.status(200).render('home.ejs',{docs:docs});
        });
    });
}

MongoClient.connect(mongourl, function (err, client){
    assert.equal(null, err);
    console.log("Connected successfully to server");
    
    const db = client.db(dbName);
    
    
    
    
    client.close();
});



app.get('/',(req,res)=>{
        handle_FindAll(req,res,req.query);
});

app.get('/report/:reportid',(req,res)=>{
        handle_Find(req,res,req.params.reportid);
		
});

app.listen(8099,function(error) {
    if(error) throw error
        console.log("Server created Successfully on PORT 8099")
})
