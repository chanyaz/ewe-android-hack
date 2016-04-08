from Patch import *

class File:
	def __init__(self, filename , fileStatus, filePatch, fileCommitId):
		self.filename = filename
		self.fileStatus = fileStatus
		self.filePatch = filePatch
		self.commitId = fileCommitId
		self.patch = Patch(self.filePatch)