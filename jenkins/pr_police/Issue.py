class Issue:
	def __init__(self, fileCommitId, filename, codelineNumber, linePositionInDiff, codeLineContent, message):
		self.fileCommitId = fileCommitId
		self.filename = filename
		self.codelineNumber = codelineNumber
		self.linePositionInDiff = linePositionInDiff
		self.codeLineContent = codeLineContent
		self.message = message