#!/usr/bin/env python

from bs4 import BeautifulSoup
import os
import urllib
import shutil
import sys

def getLastProcessedRun():
	if os.path.exists("last_processed_run"):
		with open("last_processed_run") as lastProcessedRunFile:
			return int(lastProcessedRunFile.read())
	return 8000

def saveLastProcessedRun(run):
	lastSavedProcessedRun = getLastProcessedRun()

	if run > lastSavedProcessedRun:
		with open("last_processed_run", 'w') as lastProcessedRunFile:
			lastProcessedRunFile.write(str(run))
	else:
		print "Requested to save run {run} which is not greater than last saved processed run {lastSavedProcessedRun}".format(run=run, lastSavedProcessedRun=lastSavedProcessedRun)

def deleteIfExists(filepath):
	if not os.path.exists(filepath):
		return
	if os.path.isdir(filepath):
		shutil.rmtree(filepath)
	else:
		os.remove(filepath)

def artifactsToBeProcessedSinceLastRun(server, lastProcessedRun):
	localPathToDownloadAt = os.path.join("/tmp", server.replace(".", "_") + ".txt")
	deleteIfExists(localPathToDownloadAt)
	try:
		directoryListingUrl = "http://" + server + ":8000"
		urllib.urlretrieve(directoryListingUrl, localPathToDownloadAt)
	except:
		print "Failed in downloading {directoryListingUrl}...".format(directoryListingUrl=directoryListingUrl)
		return ([], 0)

	soup = BeautifulSoup(open(localPathToDownloadAt), "html.parser")
	artifacts = ["http://" + server + ":8000/" + link.text for link in soup.find_all('a') if 'flaky.tests.frequency.txt' in link.text and int(link.text.split('-')[1]) > lastProcessedRun]
	print "Artifacts to be processed from {server} : {artifacts}".format(server=server, artifacts=artifacts)
	maxSuccessfulRun = max([int(link.text.split('-')[1]) for link in soup.find_all('a') if 'success' in link.text])

	return (artifacts, maxSuccessfulRun)

def reportFailureFrequencyDistributionPerSlave(frequenciesOfFlakyUITestsFile, testToServerToFailureCountMap):
	#Report failed tests
	for testName, serverToFailureCountMap in sorted(testToServerToFailureCountMap.items(), key=lambda x: sum(x[1].values()), reverse=True):
		totalTimesFailed = sum(serverToFailureCountMap.values())
		failureDistributionPerSlave = sorted(serverToFailureCountMap.items(), key=lambda x: x[1], reverse=True)
		print >> frequenciesOfFlakyUITestsFile, "{testName} -> {totalTimesFailed} {failureDistributionPerSlave}".format(testName=testName, totalTimesFailed=totalTimesFailed, failureDistributionPerSlave=failureDistributionPerSlave)

def main():
	testToServerToFailureCountMap = {}
	maxSuccessfulRunOnEachServer = []

	lastProcessedRun = getLastProcessedRun()
	print sys.argv[1:]
	for serverIPAndName in sys.argv[1:]:
		serverIP = serverIPAndName.split(':')[0]
		serverName = serverIPAndName.split(':')[1]

		print "Processing artifacts from {serverName}".format(serverName=serverName)
		artifactsToProcessFromThisServer, maxSuccessfulRunOnThisServer = artifactsToBeProcessedSinceLastRun(serverIP, lastProcessedRun)
		maxSuccessfulRunOnEachServer.append(maxSuccessfulRunOnThisServer)

		for artifactUrl in artifactsToProcessFromThisServer:
			#download artifact
			print "Downloading {artifactUrl}...".format(artifactUrl=artifactUrl)
			try:
				urllib.urlretrieve(artifactUrl, artifactLocalPath)
			except:
				print "Failed in downloading {artifactUrl}...".format(artifactUrl=artifactUrl)
				continue

			singleForkRunTestToFailureCountMap = eval(open(artifactLocalPath, 'r').read())

			#process artifact and record failed tests
			for failedTest in singleForkRunTestToFailureCountMap:
				if failedTest in testToServerToFailureCountMap and serverName in testToServerToFailureCountMap[failedTest]:
					testToServerToFailureCountMap[failedTest][serverName] = testToServerToFailureCountMap[failedTest][serverName] + 1
				elif failedTest in testToServerToFailureCountMap:
					testToServerToFailureCountMap[failedTest][serverName] = 1
				else:
					testToServerToFailureCountMap[failedTest] = {}
					testToServerToFailureCountMap[failedTest][serverName] = 1

	print "\n\n\nFinal List of Failed Tests and their frequency distribution per slave:"
	reportFailureFrequencyDistributionPerSlave(sys.stdout, testToServerToFailureCountMap)

	frequenciesOfFlakyUITestsFile = open('frequencies_of_flaky_ui_tests.txt', 'w')
	reportFailureFrequencyDistributionPerSlave(frequenciesOfFlakyUITestsFile, testToServerToFailureCountMap)
	frequenciesOfFlakyUITestsFile.close()

	#Save last processed run for next time!
	saveLastProcessedRun(max(maxSuccessfulRunOnEachServer))

if __name__ == "__main__":
	main()
