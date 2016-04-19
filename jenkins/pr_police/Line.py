from enum import Enum

class LineOperation(Enum):
	added = 1
	deleted = 2

class Line:
	def __init__(self, codeLineAdded, codeLineDeleted, operation, codeLineNumberInFile, linePositionInDiff):
		self.codeLineAdded = codeLineAdded.strip()
		self.codeLineDeleted = codeLineDeleted
		self.codeLineNumberInFile = codeLineNumberInFile
		self.operation = operation
		self.linePositionInDiff = linePositionInDiff