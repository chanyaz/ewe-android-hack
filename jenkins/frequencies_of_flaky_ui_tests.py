#!/usr/bin/env python

from bs4 import BeautifulSoup
import os
import urllib
import shutil
import sys
import traceback

def getLastProcessedRun():
	if os.path.exists("last_processed_run"):
		with open("last_processed_run") as lastProcessedRunFile:
			return int(lastProcessedRunFile.read())
	return 8150

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
		print "Failed in downloading {directoryListingUrl}... Stack Trace: \n{stack_trace}".format(directoryListingUrl=directoryListingUrl, stack_trace=traceback.format_exc())
		return ([], 0)

	soup = BeautifulSoup(open(localPathToDownloadAt), "html.parser")
	artifacts = ["http://" + server + ":8000/" + link.text for link in soup.find_all('a') if 'flaky.tests.frequency.txt' in link.text and int(link.text.split('-')[1]) > lastProcessedRun]
	print "Artifacts to be processed from {server} : {artifacts}".format(server=server, artifacts=artifacts)
	maxSuccessfulRun = max([int(link.text.split('-')[1]) for link in soup.find_all('a') if 'success' in link.text])

	return (artifacts, maxSuccessfulRun)

def reportFailureFrequencyDistributionPerSlaveConsole(testToServerToFailureCountMap, serverTotalRuns, failedTestToServerNameToJenkinsJobMap, serverNameToServerIPMap):
	lengthOfLongestNameTest = len(max(testToServerToFailureCountMap.keys(), key=len))
	totalTimesRun = sum(serverTotalRuns.values())
	#Report failed tests
	for testName, serverToFailureCountMap in sorted(testToServerToFailureCountMap.items(), key=lambda x: sum(x[1].values()), reverse=True):
		serverToFailurePercentMap = {}
		for serverName, failureCount in serverToFailureCountMap.items():
			serverToFailurePercentMap[serverName] = "{:.2f}%".format(100.0 * failureCount / serverTotalRuns[serverName])
		totalFailPercentage = 100.0 * sum(serverToFailureCountMap.values()) / totalTimesRun
		failureDistributionPerSlave = failureMapToArray(sorted(serverToFailurePercentMap.items(), key=lambda x: float(x[1].strip('%')), reverse=True))
		jenkinsJobArtifactURLHTML = getJenkinsJobArtifactURLHTMLFormatted(failedTestToServerNameToJenkinsJobMap[testName], serverNameToServerIPMap)
		print "{testName:<{lengthOfLongestNameTest}} -> {totalFailPercentage:.2f}% {failureDistributionPerSlave} {jenkinsJobArtifactURLHTML}".format(testName=testName, lengthOfLongestNameTest=lengthOfLongestNameTest, totalFailPercentage=totalFailPercentage, failureDistributionPerSlave=failureDistributionPerSlave, jenkinsJobArtifactURLHTML=jenkinsJobArtifactURLHTML)

def getJenkinsJobArtifactURLHTMLFormatted(latestFailureJobForTestCase, serverNameToServerIPMap):
	jenkinsJobArtifactURLHTML = ""
	for serverName in serverNameToServerIPMap:
		if serverName in latestFailureJobForTestCase:
			serverIP = serverNameToServerIPMap[serverName]
			jenkinsJobArtifactURLHTML = "{jenkinsJobArtifactURLHTML}<a href='http://{serverIP}:8000/uitests-{latestFailureJobForTestCase}-failure.tar.gz'>{serverName}({latestFailureJobForTestCase})</a> ".format(jenkinsJobArtifactURLHTML=jenkinsJobArtifactURLHTML, serverIP=serverIP, serverName=serverName, latestFailureJobForTestCase=latestFailureJobForTestCase[serverName])
	return jenkinsJobArtifactURLHTML

def reportFailureFrequencyDistributionPerSlaveHTML(testToServerToFailureCountMap, serverTotalRuns, failedTestToServerNameToJenkinsJobMap, serverNameToServerIPMap):
	flakinessReport = ""
	totalTimesRun = sum(serverTotalRuns.values())
	for testName, serverToFailureCountMap in sorted(testToServerToFailureCountMap.items(), key=lambda x: sum(x[1].values()), reverse=True):
		serverToFailurePercentMap = {}
		for serverName, failureCount in serverToFailureCountMap.items():
			serverToFailurePercentMap[serverName] = "{:.2f}%".format(100.0 * failureCount / serverTotalRuns[serverName])
		totalTimesFailed = sum(serverToFailureCountMap.values())
		totalFailPercentage = 100.0 * totalTimesFailed / totalTimesRun
		failureDistributionPerSlave = failureMapToArray(sorted(serverToFailurePercentMap.items(), key=lambda x: float(x[1].strip('%')), reverse=True))
		jenkinsJobArtifactURLHTML = getJenkinsJobArtifactURLHTMLFormatted(failedTestToServerNameToJenkinsJobMap[testName], serverNameToServerIPMap)
		flakinessReport = flakinessReport + """\n<tr class='{flakinessClass}'>
					<td>{component}</td>
					<td>{testName}</td>
					<td>{totalFailPercentage:.2f}% {failureDistributionPerSlave}</td>
					<td>{jenkinsJobArtifactURLHTML}</td>
				</tr>
		""".format(flakinessClass=flakinessClass(totalFailPercentage), component=componentFromTest(testName), testName=testName, totalFailPercentage=totalFailPercentage, failureDistributionPerSlave=failureDistributionPerSlave, jenkinsJobArtifactURLHTML=jenkinsJobArtifactURLHTML)

	with open('frequencies_of_flaky_ui_tests.html', 'w') as frequenciesOfFlakyUITestsFile:
		frequenciesOfFlakyUITestsFile.write("""<!DOCTYPE html>
		<html>
			<head>
				<title>HTML Tables</title>
				<style>
					table {
						font-family: verdana;
						border-collapse: collapse;
						border-spacing: 0;
					}
					td, th { 
						border: 1px solid #CCC;
						padding-left: 5px;
						padding-right: 5px;
						padding-top: 2px;
						padding-bottom: 2px;
					}

					tr.red {
						background: #FF0000;
					}

					tr.orange {
						background: #FF8080;
					}

					tr.yellow {
						background: #FFCCCC;
					}
				</style>
			</head>
			<body>
				<table>
					<tr>
						<th>Component</th>
						<th>Test Name</th>
						<th>Frequency and Distribution</th>
						<th>Artifacts</th>
					</tr>
					{flakinessReport}
				</table>
			</body>
		</html>""".replace("{flakinessReport}", flakinessReport))

def failureMapToArray(failureMap):
	failureArray = []
	for key, value in failureMap:
		failureArray.append("{}={}".format(key, value))
	return failureArray

def flakinessClass(totalFailPercentage):
	if totalFailPercentage >= 10:
		return "red"
	elif totalFailPercentage >= 5:
		return "orange"
	elif totalFailPercentage >= 2:
		return "yellow"

def componentFromTest(testName):
	if "hotel" in testName.lower():
		return "HOTELS"
	elif "car" in testName.lower():
		return "CARS"
	elif "lx" in testName.lower() or "gt" in testName.lower():
		return "LX"
	elif "flight" in testName.lower():
		return "FLIGHTS"
	elif "rail" in testName.lower():
		return "RAILS"
	elif "package" in testName.lower():
		return "PACKAGES"
	else:
		return ""

def main():
	testToServerToFailureCountMap = {}
	serverTotalRuns = {}
	maxSuccessfulRunOnEachServer = []

	lastProcessedRun = getLastProcessedRun()
	serverNameToServerIPMap = {}
	failedTestToServerNameToJenkinsJobMap = {}
	print sys.argv[1:]
	for serverIPAndName in sys.argv[1:]:
		serverIP = serverIPAndName.split(':')[0]
		serverName = serverIPAndName.split(':')[1]
		serverNameToServerIPMap[serverName] = serverIP
		print "Processing artifacts from {serverName}".format(serverName=serverName)
		artifactsToProcessFromThisServer, maxSuccessfulRunOnThisServer = artifactsToBeProcessedSinceLastRun(serverIP, lastProcessedRun)
		maxSuccessfulRunOnEachServer.append(maxSuccessfulRunOnThisServer)
		for artifactUrl in artifactsToProcessFromThisServer:
			artifactLocalPath = os.path.join('/tmp', artifactUrl.split('/')[-1])
			jenkinsJobName = artifactUrl.split("-")[1] + "-" + artifactUrl.split("-")[2]
			#download artifact
			print "Downloading {artifactUrl}...".format(artifactUrl=artifactUrl)
			try:
				urllib.urlretrieve(artifactUrl, artifactLocalPath)
			except:
				print "Failed in downloading {artifactUrl}... Stack Trace: \n{stack_trace}".format(artifactUrl=artifactUrl, stack_trace=traceback.format_exc())
				continue

			singleForkRunTestToFailureCountMap = eval(open(artifactLocalPath, 'r').read())

			#process artifact and record failed tests
			for failedTest in singleForkRunTestToFailureCountMap:
				#shorten the name to remove common prefix so it formats and fits nicely
				failedTest = failedTest.replace("com.expedia.bookings.test", "")
				if failedTest in testToServerToFailureCountMap and serverName in testToServerToFailureCountMap[failedTest]:
					testToServerToFailureCountMap[failedTest][serverName] = testToServerToFailureCountMap[failedTest][serverName] + 1
				elif failedTest in testToServerToFailureCountMap:
					testToServerToFailureCountMap[failedTest][serverName] = 1
				else:
					testToServerToFailureCountMap[failedTest] = {}
					testToServerToFailureCountMap[failedTest][serverName] = 1
				if failedTest not in failedTestToServerNameToJenkinsJobMap:
					failedTestToServerNameToJenkinsJobMap[failedTest] = {}
				failedTestToServerNameToJenkinsJobMap[failedTest][serverName] = jenkinsJobName

			if serverName in serverTotalRuns:
				serverTotalRuns[serverName] = serverTotalRuns[serverName] + 1
			else:
				serverTotalRuns[serverName] = 1

	print "\n\n\nFinal List of Failed Tests and their frequency distribution per slave:"
	reportFailureFrequencyDistributionPerSlaveConsole(testToServerToFailureCountMap, serverTotalRuns, failedTestToServerNameToJenkinsJobMap, serverNameToServerIPMap)
	
	reportFailureFrequencyDistributionPerSlaveHTML(testToServerToFailureCountMap, serverTotalRuns, failedTestToServerNameToJenkinsJobMap, serverNameToServerIPMap)

	#Save last processed run for next time!
	saveLastProcessedRun(max(maxSuccessfulRunOnEachServer))

if __name__ == "__main__":
	main()

