#!/usr/bin/env python3

import sys
import json

somethingWentWrongJson = [
    {
        "description": "",
        "id": "FEATURE_NAME_PLACEHOLDER1",
        "keyword": "Feature",
        "name": "FEATURE_NAME_PLACEHOLDER1",
        "line": 1,
        "uri": "",
        "elements": [
            {
                "after": [
                    {
                        "result": {
                            "status": "failed",
                            "duration": 0
                        }
                    }
                ],
                "id": "FEATURE_NAME_PLACEHOLDER1",
                "name": "SOMETHING_WENT_WRONG",
                "type": "scenario",
                "line": 2
            }
        ]
    }
]


def modifyJSONValues(featureName):
    somethingWentWrongJson[0]["id"]=featureName
    somethingWentWrongJson[0]["name"]=featureName
    somethingWentWrongJson[0]["elements"][0]["id"]=featureName

def createFileWithJSONContents(filePath):
    f=open(filePath,"w+")
    f.write(json.dumps(somethingWentWrongJson))
    f.close()


def main():
    filePath=sys.argv[1]
    featureName=sys.argv[2]
    print("Creating a dummy JSON file for "+featureName+" at "+filePath)
    modifyJSONValues(featureName)
    createFileWithJSONContents(filePath)

if __name__ == "__main__":
    main()
