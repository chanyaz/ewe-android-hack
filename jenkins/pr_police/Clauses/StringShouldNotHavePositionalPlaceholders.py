from IClause import *
import File
from Line import *
from Issue import *
import re
from StringValidationClause import *

class StringShouldNotHavePositionalPlaceholders(StringValidationClause):
	def violatingPatterns(self):
		return [r'%\d\$', r'%[^%]']

	def probableIssuesOnLineInFile(self, file, line, index):
		issueList = []
		if re.match("<string", line.codeLineAdded) and any(re.search(pattern, line.codeLineAdded) for pattern in self.violatingPatterns()):
			issueList.append(Issue(file.commitId, file.filename, line.codeLineNumberInFile, line.linePositionInDiff, line.codeLineAdded, "Use {placeholder} and Phrase to format the string with parameters"))
		return issueList