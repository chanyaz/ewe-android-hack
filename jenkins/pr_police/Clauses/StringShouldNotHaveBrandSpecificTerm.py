from IClause import *
import File
from Line import *
from Issue import *
import re
from StringValidationClause import *

class StringShouldNotHaveBrandSpecificTerm(StringValidationClause):
	def violatingPatterns(self):
		return [r'Expedia[^+]', r'Travelocity', r'Wotif', r'AirAsiaGo', r'VSC', r'LM AU & NZ']

	def probableIssuesOnLineInFile(self, file, line, index):
		issueList = []
		if re.match("<string", line.codeLineAdded) and any(re.search(pattern, line.codeLineAdded, re.I) for pattern in self.violatingPatterns()):
			issueList.append(Issue(file.commitId, file.filename, line.codeLineNumberInFile, line.linePositionInDiff, line.codeLineAdded, "String should not have brand specific term"))
		return issueList