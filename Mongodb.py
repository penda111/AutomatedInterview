from pymongo import MongoClient
from bson.objectid import ObjectId

def get_collection():
    # Provide the mongodb atlas url to connect python to mongodb using pymongo
    CONNECTION_STRING = ''

    client = MongoClient(CONNECTION_STRING)
    db = client['FYPDB']
    collection = db["resume"]
    
    return collection

def insert_report(report_json):
    # Provide the mongodb atlas url to connect python to mongodb using pymongo
    CONNECTION_STRING = ''

    client = MongoClient(CONNECTION_STRING)
    db = client['FYPDB']
    collection = db["report"]

    return collection.insert_one(report_json) 


if __name__ == "__main__":
    collection = get_collection()

    item_details = collection.find_one({'_id': ObjectId('62036ab8a38eff474d2cb291')})
    print(item_details)
    print("----")   

    name = item_details['Personal_Information']['name']
    print(name)