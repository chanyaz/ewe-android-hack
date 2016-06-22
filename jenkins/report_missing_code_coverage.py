from github3 import login
import os
import sys
parentdir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
os.sys.path.insert(0,os.path.join(parentdir, "jenkins/pr_police"))
from PullRequest import *
from Patch import *
from Line import *
import xml.etree.ElementTree as ET
from comment_pr_failures_github import createUpdateOrDeleteAutomatedFeedbackComment

def main():
    githubAccessToken = sys.argv[1]
    pullRequestId = int(sys.argv[2])
    buildURL = sys.argv[3]
    coverageFileLocations = sys.argv[4:]
    githubOrganization = 'ExpediaInc'
    githubRepository = 'ewe-android-eb'

    github = login(token=githubAccessToken)
    repo = github.repository(githubOrganization, githubRepository)
    pullRequest = PullRequest(repo, pullRequestId)
    pullRequest.scanPullRequest()
    missingCoverageReports = reportMissingCoverageInPRFiles(pullRequest, coverageFileLocations, buildURL)
    issue = repo.issue(pullRequestId)
    if missingCoverageReports:
        comment = "New code added in this PR is not being fully covered by tests. Code coverage reports showcasing the misses: \n"
        for report in missingCoverageReports:
            comment += report + "\n"
        print "Found missing code coverage. Adding a comment to PR."
        createUpdateOrDeleteAutomatedFeedbackComment(githubAccessToken, githubOrganization, githubRepository, pullRequestId, 'coverage', comment, "")
        issue.add_labels('needs-tests')
        return 1
    else:
        print "No missing code coverage found."
        comment = "Hurray!!! All the changes seem to be covered with Unit tests."
        createUpdateOrDeleteAutomatedFeedbackComment(githubAccessToken, githubOrganization, githubRepository, pullRequestId, 'coverage', comment, "")
        issue.remove_label('needs-tests')
        return 0


def reportMissingCoverageInPRFiles(pullRequest, coverageFileLocations, buildURL):
    missingCoverageReports = []

    coverageRoots = [ET.parse(coverageFileLocation).getroot() for coverageFileLocation in coverageFileLocations]
    coverageXmlData = reduce(lambda rootOne, rootTwo: [rootOne.append(rootTwo), rootOne][1], coverageRoots)

    for file in pullRequest.pr.files():
        justFileName = file.filename.split("/").pop()
        coverageXmlDataForFile = coverageXmlData.find(".//package/sourcefile[@name='{source_filename}']".format(source_filename=justFileName))

        if coverageXmlDataForFile is not None:
            patch = Patch(file.patch)
            for line in patch.fileLines:
                if line.operation == LineOperation.added:
                    lineFound = coverageXmlDataForFile.find("./line[@nr='{line_number}']".format(line_number=line.codeLineNumberInFile))
                    if lineFound is not None:
                        missedInstrunctions = int(lineFound.get("mi"))
                        missedBranches = int(lineFound.get("mb"))
                        if missedInstrunctions > 0 or missedBranches > 0:
                            reportURL = getHtmlReportPathFromFileName(str(file.filename), buildURL)
                            commentTemplate = "[{fileName}]({reportURL})"
                            missingCoverageReports.append(commentTemplate.format(fileName=file.filename.split("/").pop(), reportURL=reportURL))
                            break

    return missingCoverageReports

def getHtmlReportPathFromFileName(fileName, buildURL):
    startingPathsAndPublishDirectoryForSubprojects = {"lib/ExpediaBookings/src/main/java/" : "Jacoco_Expediabookings", "project/src/main/java/" : "Jacoco_project"}

    for key, value in startingPathsAndPublishDirectoryForSubprojects.iteritems():
        if key in fileName:
            return "{buildURL}/{publishDirectory}/{filePath}".format(buildURL=buildURL, publishDirectory=startingPathsAndPublishDirectoryForSubprojects[key], filePath=formatFilePathToReportPath(fileName, key))

# Example :
# fileName = project/src/main/java/com/expedia/bookings/widget/NewLaunchLobViewModel.java
# path = project/src/main/java/
# Output = com.expedia.bookings.widget/NewLaunchLobViewModel.java.html
def formatFilePathToReportPath(fileName, path):
    packagePathAndFileNameString = fileName.replace(path, "")
    packagePathAndFileNameArray = packagePathAndFileNameString.split("/")
    coverageFileName = "/{coverageFileName}.html".format(coverageFileName=packagePathAndFileNameArray.pop())
    filePath = ".".join(packagePathAndFileNameArray)
    return filePath + coverageFileName

if __name__ == "__main__":
    sys.exit(main())
