import os
import sys
from github3 import login
from pr_utils import pingPRAuthors, prUrl
from comment_pr_failures_github import createUpdateOrDeleteAutomatedFeedbackComment

def pingFlavorTestsFailed(githubAccessToken, githubOrganization, githubRepository, prPullId, hipchatAccessToken):
        github = login(token=githubAccessToken)
        repo = github.repository(githubOrganization, githubRepository)
        pr = repo.pull_request(prPullId)
        messageToBePinged = "Compilation failed for PR <a href='{pr_url}'>{pr_title}</a>.<br>Failure details injected as comment in the PR.".format(pr_title=pr.title, pr_url=prUrl(pr))
        pingPRAuthors(pr, hipchatAccessToken, messageToBePinged)

def main():
    githubAccessToken = sys.argv[1]
    githubRepoId = sys.argv[2]
    prPullId = sys.argv[3]
    hipchatAccessToken = sys.argv[4]
    buildStatus = sys.argv[5]
    prBuilderType = "flavors"
    errorMessage = ""
    githubRepo = githubRepoId.split("/")
    githubOrganization = githubRepo[0]
    githubRepository = githubRepo[1]
    flavorsFeedbackBotErrors = '/tmp/flavorsFeedbackBotErrors.txt'

    if buildStatus != 0 and os.path.exists(flavorsFeedbackBotErrors):
        with open(flavorsFeedbackBotErrors) as compilation_feedback:
            errorMessages = compilation_feedback.readlines()
            #ignore log4j errors at start
            while 'log4j:ERROR' in errorMessages[0]:
                    errorMessages.remove(errorMessages[0])
            #ignore unneeded lines at end
            endIndex = len(errorMessages)
            if 'FAILURE: Build failed with an exception.\n' in errorMessages:
                endIndex = errorMessages.index('FAILURE: Build failed with an exception.\n')
                errorMessage = "".join(errorMessages[:endIndex]).strip()
            print errorMessage
    createUpdateOrDeleteAutomatedFeedbackComment(githubAccessToken, githubOrganization, githubRepository, prPullId, prBuilderType, errorMessage, "java")

    if errorMessage != "":
        #Ping the authors that the PR Builder failed
        pingFlavorTestsFailed(githubAccessToken, githubOrganization, githubRepository, prPullId, hipchatAccessToken)

if __name__ == "__main__":
    main()
