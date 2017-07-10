#!/usr/bin/env python
# 
# Script for renaming assets and moving back to their respective density folders.
#
# This script should be used with AssetAggregator. AssetAggregator copies files having given name, 
# in different density folders, to a common folder. It also renames them according to density. 
# AssetsSorter moves them to respective density folder and renames back to original.  
#
# 1. Install python from https://www.python.org/downloads/.
# 3. In console, go to project checkout directory e.g. d:/expediaAndroid/ewe-android-eb
# 4. For sorting all assets in "D:/expediaAndroid/Assets/aggregated" use "py tools/assetsSorter.py"
# 5. Assets will be copied in "D:/expediaAndroid/assets/extracted".

import os
import shutil
from shutil import move

sourcePath = '..'+os.path.sep+'assets'+os.path.sep+'aggregated'
destPath = '..'+os.path.sep+'assets'+os.path.sep+'extracted'

print ("")
print("--------------------------------------------------------")
print ("Usage py tools/assetsSorter.py")
print("---------------------------------------------------------")
print("")
    
if os.path.exists(destPath):
    shutil.rmtree(destPath)
postfixes=["","xxxhdpi","xxhdpi","xhdpi","hdpi","mdpi","ldpi","xxxhdpi-v21","xxhdpi-v21","xhdpi-v21","hdpi-v21","mdpi-v21","ldpi-v21","sw600dp-xhdpi","sw600dp-xxhdpi"]
fileExtensions = ("png", "jpg", "jpeg", "xml", "9.png", "webp")
delim="["

files = [ f for f in os.listdir(sourcePath) if os.path.isfile(os.path.join(sourcePath,f)) ]
for file in files:
    fileNameWithoutExtension, fileExtension = os.path.splitext(file)
    if fileExtension.endswith("png"):
        newName, newExtention= os.path.splitext(fileNameWithoutExtension)
        if len(newExtention)!=0:
            fileExtension=newExtention+fileExtension
            fileNameWithoutExtension = newName
            print("name: ",fileNameWithoutExtension,"Ext:",fileExtension)
        
    for postfix in postfixes:
        
        if fileNameWithoutExtension.endswith(delim+postfix):
            originalFileName = fileNameWithoutExtension.split(delim+postfix)[0]
            if len(postfix)==0:
                postfix = "drawable"
            else:
                postfix = "drawable-"+postfix

            destinationDir = destPath+os.path.sep+postfix
            if not os.path.exists(destinationDir):
                os.makedirs(destinationDir)
            sourceFileName = sourcePath+os.path.sep+file
            destinationFileName = destinationDir+os.path.sep+originalFileName+fileExtension
            move(sourceFileName, destinationFileName)
            print("File", file, "Moved to:", destinationFileName)