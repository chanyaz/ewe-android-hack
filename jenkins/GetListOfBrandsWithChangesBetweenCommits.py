import os
import subprocess
import sys
from github3 import login
GITHUB_TOKEN="0a1f692d47819eec1349e990240525233a12b4fd"

def getBrandList(commitFirst, commitSecond):
    filepath = subprocess.check_output(['git', 'diff', '--name-only', commitFirst + '..' + commitSecond])
    filepathList = filepath.split("\n")
    appList = []
    
    for fileValue in filepathList:
        if "/src/" in fileValue:
           brandName = fileValue.split("src/",1)[1].split("/",1)[0]
           if brandName not in appList:
                appList.append(brandName)
    if len(appList) > 0:
        if "Main " in appList:
            print ("")
        else:
            print (" ".join(appList))

if __name__ == "__main__":
    githubToken = GITHUB_TOKEN
    commitFirst = sys.argv[1]
    commitSecond = sys.argv[2]
    getBrandList(commitFirst, commitSecond)