#!/usr/bin/env python

import os
import json
import objectpath
import glob
import sys

def main():
	testToFailureCountMap = {}

	forkOutputDir = sys.argv[1]
	#process artifact and record failed tests
	for forkTestRunOutputFileName in glob.glob(os.path.join(forkOutputDir, 'expedia/debug/summary/fork-*.json')):
		print "Processing {forkTestRunOutputFileName}...".format(forkTestRunOutputFileName=forkTestRunOutputFileName)
		with open(forkTestRunOutputFileName) as forkTestRunOutputFile:
			forkTestRunOutputJsonData = json.load(forkTestRunOutputFile)

			forkTestRunOutputJsonTree = objectpath.Tree(forkTestRunOutputJsonData)
			forkTestRunFailureMetadataList = forkTestRunOutputJsonTree.execute('$.poolSummaries[0].testResults[@.testClass and @.testMethod and @.testMetrics]')
			for forkTestRunFailureMetadata in forkTestRunFailureMetadataList:
				failedTest = forkTestRunFailureMetadata['testClass'] + "." + forkTestRunFailureMetadata['testMethod']
				testToFailureCountMap[failedTest] = forkTestRunFailureMetadata['testMetrics']['totalFailureCount']

	print "\n\n\nFinal List of Failed Tests:"
	print testToFailureCountMap

	if len(testToFailureCountMap) > 0:
		filepathToWriteTo = sys.argv[2]
		with open(filepathToWriteTo, "w") as fileToWriteTo:
			fileToWriteTo.write(str(testToFailureCountMap))

if __name__ == "__main__":
	main()
