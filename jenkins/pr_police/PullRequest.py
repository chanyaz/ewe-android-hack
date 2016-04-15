import sys
from File import File

class PullRequest:
	def __init__(self, repo, prId):
		self.repo = repo
		self.prId = prId
		self.files = []
		self.issueList = []
		self.pr = repo.pull_request(prId)

	def isOkayToProcess(self, maxCommits):
		commitCount = self.pr.commits
		return commitCount < maxCommits

	def addIssueList(self, issues):
		self.issueList.extend(issues)

	def filesInPullRequest(self):
		filepaths = []
		for file in self.pr.files():
			filepaths.append(file.filename)
		return filepaths

	def scanPullRequest(self):
		self.files = []
		for file in self.pr.files():
			filename = file.filename
			fileStatus = file.status
			filePatch = file.patch
			#fileCommitId is delay loaded, if required!
			fileCommitId = None
			self.files.append(File(filename, fileStatus, filePatch, fileCommitId))

	def ensureCommitIdOfFileIsFilledIn(self, filename):
		#Find 'File' corresponding to 'filename' and ensure its commitId is set!
		for file in self.files:
			if file.filename == filename:
				if file.commitId == None:
					file.commitId = self.getCommitId(file.filename)
				return file.commitId
		return ""

	def getCommitId(self, filename):
		fileCommitId = ""
		for commitId in self.pr.commits():
			commit = self.repo.commit(commitId.sha)
			for file in commit.files:
				if file['filename'] == filename:
					fileCommitId = commitId.sha
		return fileCommitId
