#!/usr/bin/env python2.7

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


def format_checkstyle_errors(errors):
    if len(errors) == 0:
        return ""
    msg = "{number_of_errors} checkstyle errors:\n".format(number_of_errors=len(errors))
    for error in errors:
        msg += "{file}:{line}:{message}\n".format(file=error["file"], line=error["line"], message=error["message"])
    return msg


def read_and_format_checkstyle_error_message(checkstyleReportFileList):
    errors = []
    for filePath in checkstyleReportFileList:
        if not os.path.exists(filePath):
            continue
        with open(filePath) as checkstyleErrorXmlFile:
            checkstyle_xml = etree.parse(checkstyleErrorXmlFile)
            files = checkstyle_xml.xpath('//file[descendant::error]')
            for file_xml in files:
                lines = file_xml.xpath('error/@line')
                messages = file_xml.xpath('error/@message')
                for (line, message) in zip(lines, messages):
                    error = {"file": file_xml.attrib["name"], "line": line, "message": message}
                    errors.append(error)
    return format_checkstyle_errors(errors)


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


def pingUnitTestsFailed(githubAccessToken, githubOrganization, githubRepository, prPullId, slack_access_token, additional_content = None):
    github = login(token=githubAccessToken)
    repo = github.repository(githubOrganization, githubRepository)
    pr = repo.pull_request(prPullId)
    if additional_content:
        slack_message = "Unit Tests failed {pr_url}.\n{additional_content}".format(pr_title=pr.title, pr_url=prUrl(pr), additional_content=additional_content)
    else:
        slack_message = "Unit Tests failed {pr_url}.\nFailure details injected as comment in the PR.".format(pr_title=pr.title, pr_url=prUrl(pr))
    pingPRAuthors(pr, slack_access_token, slack_message)

def main():
    githubAccessToken = sys.argv[1]
    githubRepoId = sys.argv[2]
    prPullId = sys.argv[3]
    slack_access_token = sys.argv[4]
    prBuilderType = "unit"
    githubRepo = githubRepoId.split("/")
    githubOrganization = githubRepo[0]
    githubRepository = githubRepo[1]
    checkstyleReportFileList = ['./lib/ExpediaBookings/build/reports/checkstyle/main.xml', './lib/ExpediaBookings/build/reports/checkstyle/test.xml', './project/build/reports/checkstyle/checkstyle.xml']
    unitTestReportFileList = ['./lib/ExpediaBookings/build/test-results/TEST-com.expedia.*', './project/build/test-results/TEST-com.expedia.*']
    lintTestReportFilePath = ['./project/build/reports/lint-results-expediaDebug.xml', './project/build/reports/lint-results-expediaRelease.xml']
    kotlinUnusedResourcesReportFileName = './project/build/outputs/kotlin-unused-resources.txt'

    unittestsErrorMessage = formatUnitTestErrorMessage(unitTestReportFileList) + read_and_format_checkstyle_error_message(checkstyleReportFileList) + formatLintErrorMessage(lintTestReportFilePath) + formatKotlinUnusedResourcesMessage(kotlinUnusedResourcesReportFileName)
    print unittestsErrorMessage
    createUpdateOrDeleteAutomatedFeedbackComment(githubAccessToken, githubOrganization, githubRepository, prPullId, prBuilderType, unittestsErrorMessage, "text")

    if unittestsErrorMessage:
        #Ping the authors that the PR Builder failed
        pingUnitTestsFailed(githubAccessToken, githubOrganization, githubRepository, prPullId, slack_access_token, additional_content=unittestsErrorMessage)


if __name__ == "__main__":
    main()
