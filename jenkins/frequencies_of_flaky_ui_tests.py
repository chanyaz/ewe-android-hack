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

def reportFailureFrequencyDistributionPerSlaveConsole(testToServerToFailureCountMap):
	lengthOfLongestNameTest = len(max(testToServerToFailureCountMap.keys(), key=len))
	#Report failed tests
	for testName, serverToFailureCountMap in sorted(testToServerToFailureCountMap.items(), key=lambda x: sum(x[1].values()), reverse=True):
		totalTimesFailed = sum(serverToFailureCountMap.values())
		failureDistributionPerSlave = sorted(serverToFailureCountMap.items(), key=lambda x: x[1], reverse=True)
		print "{testName} -> {totalTimesFailed} {failureDistributionPerSlave}".format(testName=testName.ljust(lengthOfLongestNameTest), totalTimesFailed=totalTimesFailed, failureDistributionPerSlave=failureDistributionPerSlave)

def reportFailureFrequencyDistributionPerSlaveHTML(testToServerToFailureCountMap):
	flakinessReport = ""
	for testName, serverToFailureCountMap in sorted(testToServerToFailureCountMap.items(), key=lambda x: sum(x[1].values()), reverse=True):
		totalTimesFailed = sum(serverToFailureCountMap.values())
		failureDistributionPerSlave = sorted(serverToFailureCountMap.items(), key=lambda x: x[1], reverse=True)
		flakinessReport = flakinessReport + """\n<tr class='{flakinessClass}'>
					<td>{component}</td>
					<td>{testName}</td>
					<td>{totalTimesFailed} {failureDistributionPerSlave}</td>
				</tr>
		""".format(flakinessClass=flakinessClass(totalTimesFailed), component=componentFromTest(testName), testName=testName, totalTimesFailed=totalTimesFailed, failureDistributionPerSlave=failureDistributionPerSlave)

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
					</tr>
					{flakinessReport}
				</table>
			</body>
		</html>""".replace("{flakinessReport}", flakinessReport))

def flakinessClass(totalTimesFailed):
	if totalTimesFailed >= 8:
		return "red"
	elif totalTimesFailed >= 4:
		return "orange"
	elif totalTimesFailed >= 2:
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
			artifactLocalPath = os.path.join('/tmp', artifactUrl.split('/')[-1])
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

	print "\n\n\nFinal List of Failed Tests and their frequency distribution per slave:"
	reportFailureFrequencyDistributionPerSlaveConsole(testToServerToFailureCountMap)
	
	reportFailureFrequencyDistributionPerSlaveHTML(testToServerToFailureCountMap)

	#Save last processed run for next time!
	saveLastProcessedRun(max(maxSuccessfulRunOnEachServer))

if __name__ == "__main__":
	main()

