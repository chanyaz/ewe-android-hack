from IClause import *
import File
from Line import *
from Issue import *
import re
from StringValidationClause import *

class TemplatedStringShouldBeSuffixedWithTEMPLATE(StringValidationClause):
	def placeholderPatterns(self):
		return [r'%\d\$', r'%[^%]', r'\{[a-z][a-z_]+\}']

	def probableIssuesOnLineInFile(self, file, line, index):
		issueList = []
		if re.match("<string", line.codeLineAdded) and (any(re.search(pattern, line.codeLineAdded) for pattern in self.placeholderPatterns()) and not re.search(r'_TEMPLATE">', line.codeLineAdded)):
			issueList.append(Issue(file.commitId, file.filename, line.codeLineNumberInFile, line.linePositionInDiff, line.codeLineAdded, "Templated strings should be suffixed with `_TEMPLATE`"))
		return issueList