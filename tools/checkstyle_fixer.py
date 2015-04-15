#!/usr/bin/python

try:
	import xml.etree.cElementTree as ET
except ImportError:
	import xml.etree.ElementTree as ET

import subprocess
import os
import re
import sys

def handleFaultyPatterns (filePath, errorLine, faultyPattern, correctReplacement, isRegex):
	file = open(filePath, 'r')
	lines = file.readlines()
	file.close()
	file = open(filePath, 'w')
	lineIndex = 0;
	for line in lines:
		lineIndex = lineIndex + 1
		if lineIndex == errorLine:
			if isRegex:
				line = re.sub(faultyPattern, correctReplacement, line)
			else:
				line = line.replace(faultyPattern, correctReplacement)
		file.write(line)
	file.close()

def handleUnusedImports (filePath, linesToBeDeletedInFile):
	file = open(filePath, 'r')
	lines = file.readlines()
	file.close()
	file = open(filePath, 'w')
	lineIndex = 0;
	for line in lines:
		lineIndex = lineIndex + 1
		if lineIndex not in linesToBeDeletedInFile:
			file.write(line)
	file.close()

def fixCheckstyleErrors (checkStyleOutputFiles):
	for checkStyleOutputFile in checkStyleOutputFiles:
		if not os.path.isfile(checkStyleOutputFile):
			continue
		tree = ET.ElementTree(file=checkStyleOutputFile)
		for fileElem in tree.iter(tag='file'):
			filePath = fileElem.attrib['name']
			linesToBeDeletedInFile = [];
			for errorElem in fileElem.iter():
				if errorElem.tag == "error":
					message = errorElem.attrib['message']
					errorLine = int(errorElem.attrib['line'])
					if message == "'if' is not followed by whitespace.":
						handleFaultyPatterns (filePath, errorLine, "if(", "if (", False)
					elif message == "'for' is not followed by whitespace.":
						handleFaultyPatterns (filePath, errorLine, "for(", "for (", False)
					elif message == "Line has trailing spaces.":
						handleFaultyPatterns (filePath, errorLine, "[ \t]+$", "", True)
					elif message.startswith("Unused import - ") or message.startswith("Duplicate import to"):
						linesToBeDeletedInFile.append(errorLine)

			#Handling unused imports leads to line removals, altering line numbers, so process this separately, at last
			if len(linesToBeDeletedInFile) > 0:
				handleUnusedImports (filePath, linesToBeDeletedInFile)

if __name__ == "__main__":
	allCheckStyleOutputFiles = ["lib/ExpediaBookings/build/reports/checkstyle/main.xml", 
								"lib/ExpediaBookings/build/reports/checkstyle/test.xml", 
								"robolectric/build/reports/checkstyle/test.xml", 
								"project/build/reports/checkstyle/checkstyle.xml"]
	
	if len(sys.argv) == 1:
		#Trigger the "Try & Do it for me" behavior by first capturing the fresh checkstyle errors
		subprocess.call(["./gradlew", "checkstyle", ":lib:ExpediaBookings:checkstyleMain", ":lib:ExpediaBookings:checkstyleTest", ":robolectric:checkstyleTest"])
		fixCheckstyleErrors(allCheckStyleOutputFiles)
	else:
		#We were passed in the specific files to check errors from and fix
		fixCheckstyleErrors(sys.argv[1:])
