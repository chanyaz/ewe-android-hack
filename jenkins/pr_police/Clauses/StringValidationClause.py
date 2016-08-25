from IClause import *
from Line import *
from Issue import *
import re
import operator

class StringValidationClause(IClause):
	def probableIssuesOnLineInFile(self, file, line, index):
		#Derived clauses should implement this method and return issues found on `line` in `file`
		return []

	def wantsToScanFile(self, filepath):
		return True if re.search("values/strings\.xml", filepath, re.I) else False

	def probableIssues(self, file):
		issueList = []
		if self.wantsToScanFile(file.filename):
			issueLists = [self.probableIssuesOnLineInFile(file, line, index) for index, line in enumerate(file.patch.fileLines) if line.operation == LineOperation.added]
			if len(issueLists) > 0:
				issueList = reduce(operator.add, issueLists)
		return issueList