import sys
from File import File

class PullRequest:
	def __init__(self, repo, prId):
		self.repo = repo
		self.prId = prId
		self.files = []
		self.pr = repo.pull_request(prId)	
				
	def addIssueList(self, issues):
		self.issueList = issues

	def scanPullRequest(self):
		for file in self.pr.iter_files():
			filename = file.filename
			fileStatus = file.status
			filePatch = file.patch
			fileCommitId = self.getCommitId(filename)			
			self.files.append(File(filename, fileStatus, filePatch, fileCommitId))

	def getCommitId(self, filename):
		fileCommitId = None
		for commitId in self.pr.iter_commits():
			commit = self.repo.commit(commitId.sha)
			for file in commit.files:
				if file['filename'] == filename:
					fileCommitId = commitId.sha
		return fileCommitId
