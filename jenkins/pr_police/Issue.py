from enum import Enum

class IssueType(Enum):
	warning = 1
	error = 2

class Issue:
	def __init__(self, fileCommitId, filename, codelineNumber, linePositionInDiff, codeLineContent, message, issueType = IssueType.error ):
		self.fileCommitId = fileCommitId
		self.filename = filename
		self.codelineNumber = codelineNumber
		self.linePositionInDiff = linePositionInDiff
		self.codeLineContent = codeLineContent
		self.message = message
		self.issueType = issueType