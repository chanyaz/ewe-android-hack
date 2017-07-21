import sys
import glob
import objectpath
import json
from github3 import login
from comment_pr_failures_github import createUpdateOrDeleteAutomatedFeedbackComment
from pr_utils import pingPRAuthors, prUrl

def scanAndProcessForkTestRunOutputJsonData(githubAccessToken, githubOrganization, githubRepository, prPullId, prBuilderType, forkTestRunOutputJsonData):
    forkTestRunOutputJsonTree = objectpath.Tree(forkTestRunOutputJsonData)
    forkTestRunMetadataList = forkTestRunOutputJsonTree.execute('$.poolSummaries[0].testResults[@.testClass and @.failureTrace]')
    uiErrorMsg = ""
    failedTestCaseCount = 0
    for forkTestRunMetadata in forkTestRunMetadataList:
        failureTraceMessage = forkTestRunMetadata["failureTrace"]
        failureTraceMessage = failureTraceMessage.replace("\u003d","=")
        failureTraceMessage = failureTraceMessage.replace("\u003e",">")
        failureTraceMessage = failureTraceMessage.replace("u003c","<")
        failureTraceMessage = failureTraceMessage.replace("\\n","\n")
        errorMessage = forkTestRunMetadata['testClass'] + "." + forkTestRunMetadata['testMethod'] +":" + failureTraceMessage
        failedTestCaseCount += 1
        error_no = "ERROR " + str(failedTestCaseCount)
        uiErrorMsg = uiErrorMsg + "**" + error_no + "**" + "\n\n" + errorMessage + "\n\n"
    createUpdateOrDeleteAutomatedFeedbackComment(githubAccessToken, githubOrganization, githubRepository, prPullId, prBuilderType, uiErrorMsg, "java")
    return failedTestCaseCount

def main():
    githubAccessToken = sys.argv[1]
    githubRepoId = sys.argv[2]
    prPullId = sys.argv[3]
    hipchatAccessToken = sys.argv[4]
    prBuilderType = "ui"
    githubRepo = githubRepoId.split("/")
    githubOrganization = githubRepo[0]
    githubRepository = githubRepo[1]
    print prBuilderType
    print prPullId
    status = 0
    for name in glob.glob('./project/build/fork/expedia/debug/summary/fork-*.json'):
        with open(name) as errorJsonFile:
            forkTestRunOutputJsonData = json.load(errorJsonFile)
            status = scanAndProcessForkTestRunOutputJsonData(githubAccessToken, githubOrganization, githubRepository, prPullId, prBuilderType, forkTestRunOutputJsonData)
            break

    if status != 0:
        #Ping the authors that the PR Builder failed
        github = login(token=githubAccessToken)
        repo = github.repository(githubOrganization, githubRepository)
        pr = repo.pull_request(prPullId)
        messageToBePinged = "UI Tests failed for PR <{pr_url}|{pr_title}>.\nFailure details injected as comment in the PR.".format(pr_title=pr.title, pr_url=prUrl(pr))
        pingPRAuthors(pr, hipchatAccessToken, messageToBePinged)

    return status

if __name__ == "__main__":
    sys.exit(main())
