from IClause import *
import File
from Line import *
import Patch
from Issue import *
import re

class StringShouldNotHaveBrandSpecificTerm(IClause):
	def __init__(self):
		pass

	def violatingPatterns(self):
		return [r'Expedia[^+]', r'Travelocity', r'Wotif', r'AirAsiaGo', r'VSC', r'LM AU & NZ']

	def probableIssues(self, file, issueList):
		if re.search("strings\.xml", file.filename, re.I):
			for line in file.patch.fileLines:
				if line.operation == LineOperation.added:
					if re.match("<string", line.codeLineAdded) and any(re.search(pattern, line.codeLineAdded, re.I) for pattern in self.violatingPatterns()):
						issueList.append(Issue(file.commitId, file.filename, line.codeLineNumberInFile, line.codeLineAdded, "String should not have brand specific term"))
		return issueList