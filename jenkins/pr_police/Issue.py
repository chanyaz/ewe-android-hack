class Issue:
	def __init__(self, fileCommitId, filename, codelineNumber, codeLineContent, message):
		self.fileCommitId = fileCommitId
		self.filename = filename
		self.codelineNumber = codelineNumber
		self.codeLineContent = codeLineContent
		self.message = message