import sys
import glob
import os
from lxml import etree
from github3 import login
from comment_pr_failures_github import createUpdateOrDeleteAutomatedFeedbackComment
from pr_utils import pingPRAuthors, prUrl

def scanAndProcessLintTestRunOutputXmlData(lintTestRunOutputXmlData):
    lintErrorMessage = ""
    linterrorIssueSeverity = lintTestRunOutputXmlData.xpath('//issue/@severity')
    linterrorIssueMessages = lintTestRunOutputXmlData.xpath('//issue/@message')
    linterrorIssueExplanations = lintTestRunOutputXmlData.xpath('//issue/@explanation')
    errorCount = 0
    for linterrorCount in range(0,len(linterrorIssueMessages)):
        if linterrorIssueSeverity[linterrorCount] == "Error":
            lintIssueLocation = lintTestRunOutputXmlData.xpath('//issue['+str(linterrorCount+1)+']/location/@file')
            lintErrorMessage = lintErrorMessage + "**ERROR" + str(errorCount+1) + "**\n\n" + "Lint Error: " +  linterrorIssueMessages[linterrorCount] + "\nExplanation: " + linterrorIssueExplanations[linterrorCount]+ "\nLocation: "+ ','.join(lintIssueLocation) + "\n\n"
            errorCount += 1
    return lintErrorMessage

def extractCheckstyleErrorMessage(errorMessageList, errorLineList, errorFileNameList):
    return reduce(lambda accumulator, (index, fileName, line, message): accumulator + "**ERROR " + str(index+1) + "**\n\n" + "Checkstyle Error in file " + fileName + "on line:" + line + " is: " + message + "\n\n", zip(range(len(errorLineList)), errorFileNameList, errorLineList, errorMessageList), "")


def extractUnitTestErrorMessage(failureMessageList, failureClassList, failureFunctionList):
    return reduce(lambda accumulator, (index, failureClass, failureFunction, message): accumulator + "**ERROR " + str(index+1) + "**\n\n" + failureClass + "." + failureFunction + ":" + message + "\n\n", zip(range(len(failureMessageList)), failureClassList, failureFunctionList, failureMessageList), "")

def formatKotlinUnusedResourcesMessage(kotlinUnusedResourcesReportFileName):
    unusedResourcesErrorMsg = ""
    if os.path.exists(kotlinUnusedResourcesReportFileName):
        with open(kotlinUnusedResourcesReportFileName) as unusedResourcesReport:
            unusedResourcesErrorMsg = unusedResourcesReport.read()
    return unusedResourcesErrorMsg

def formatLintErrorMessage(lintTestReportFileList):
    lintTestErrorMsg = ""
    for filepath in lintTestReportFileList:
        if not os.path.exists(filepath):
            continue
        with open(filepath) as lintErrorXmlFile:
            lintTestRunXmlData = etree.parse(lintErrorXmlFile)
            lintTestErrorMsg = scanAndProcessLintTestRunOutputXmlData(lintTestRunXmlData)
    return lintTestErrorMsg

def formatCheckstyleErrorMessage(checkstyleReportFileList):
    errorFileNameList = []
    errorMessageList = []
    errorLineList = []
    for filePath in checkstyleReportFileList:
        if not os.path.exists(filePath):
            continue
        with open(filePath) as checkstyleErrorXmlFile:
            checkstyleRunOutputXmlData = etree.parse(checkstyleErrorXmlFile)
            errorFileNameList.extend(checkstyleRunOutputXmlData.xpath('//file[descendant::error]/@name'))
            errorMessageList.extend(checkstyleRunOutputXmlData.xpath('//file/error/@message'))
            errorLineList.extend(checkstyleRunOutputXmlData.xpath('//file/error/@line'))

    return extractCheckstyleErrorMessage(errorMessageList, errorLineList, errorFileNameList)

def formatUnitTestErrorMessage(unitTestReportFilePatternList):
    failureFunctionList = []
    failureMessageList = []
    failureClassList = []
    unitTestReportFileList = reduce(lambda x,y: x+y, map(lambda x: glob.glob(x), unitTestReportFilePatternList))
    for filePath in unitTestReportFileList:
        with open(filePath) as errorXmlFile:
            unitTestRunOutputXmlData = etree.parse(errorXmlFile)
            failureMessageList.extend(unitTestRunOutputXmlData.xpath('//failure/text()'))
            failureFunctionList.extend(unitTestRunOutputXmlData.xpath('//testcase[descendant::failure]/@name'))
            failureClassList.extend(unitTestRunOutputXmlData.xpath('//testcase[descendant::failure]/@classname'))

    return extractUnitTestErrorMessage(failureMessageList, failureClassList, failureFunctionList)


def pingUnitTestsFailed(githubAccessToken, githubOrganization, githubRepository, prPullId, hipchatAccessToken):
    github = login(token=githubAccessToken)
    repo = github.repository(githubOrganization, githubRepository)
    pr = repo.pull_request(prPullId)
    messageToBePinged = "Unit Tests failed for PR <a href='{pr_url}'>{pr_title}</a>.<br>Failure details injected as comment in the PR.".format(pr_title=pr.title, pr_url=prUrl(pr))
    pingPRAuthors(pr, hipchatAccessToken, messageToBePinged)

def main():
    githubAccessToken = sys.argv[1]
    githubRepoId = sys.argv[2]
    prPullId = sys.argv[3]
    hipchatAccessToken = sys.argv[4]
    prBuilderType = "unit"
    githubRepo = githubRepoId.split("/")
    githubOrganization = githubRepo[0]
    githubRepository = githubRepo[1]
    checkstyleReportFileList = ['./lib/ExpediaBookings/build/reports/checkstyle/main.xml', './lib/ExpediaBookings/build/reports/checkstyle/test.xml', './project/build/reports/checkstyle/checkstyle.xml']
    unitTestReportFileList = ['./lib/ExpediaBookings/build/test-results/TEST-com.expedia.*', './project/build/test-results/TEST-com.expedia.*']
    lintTestReportFilePath = ['./project/build/reports/lint-results-expediaDebug.xml', './project/build/reports/lint-results-expediaRelease.xml']
    kotlinUnusedResourcesReportFileName = './project/build/outputs/kotlin-unused-resources.txt'

    unittestsErrorMessage = formatUnitTestErrorMessage(unitTestReportFileList) + formatCheckstyleErrorMessage(checkstyleReportFileList) + formatLintErrorMessage(lintTestReportFilePath) + formatKotlinUnusedResourcesMessage(kotlinUnusedResourcesReportFileName)
    print unittestsErrorMessage
    createUpdateOrDeleteAutomatedFeedbackComment(githubAccessToken, githubOrganization, githubRepository, prPullId, prBuilderType, unittestsErrorMessage, "java")

    if unittestsErrorMessage != "":
        #Ping the authors that the PR Builder failed
        pingUnitTestsFailed(githubAccessToken, githubOrganization, githubRepository, prPullId, hipchatAccessToken)


if __name__ == "__main__":
    main()
