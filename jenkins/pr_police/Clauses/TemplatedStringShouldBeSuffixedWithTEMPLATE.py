from IClause import *
import File
from Line import *
import Patch
from Issue import *
import re

class TemplatedStringShouldBeSuffixedWithTEMPLATE(IClause):
	def __init__(self):
		pass		

	def placeholderPatterns(self):
		return [r'%\d\$', r'%[^%]', r'\{[a-z][a-z_]+\}']

	def probableIssues(self, file, issueList):
		if re.search("strings\.xml", file.filename, re.I):
			for line in file.patch.fileLines:
				if line.operation == LineOperation.added:
					if re.match("<string", line.codeLineAdded) and (any(re.search(pattern, line.codeLineAdded) for pattern in self.placeholderPatterns()) and not re.search(r'_TEMPLATE">', line.codeLineAdded)):
						issueList.append(Issue(file.commitId, file.filename, line.codeLineNumberInFile, line.codeLineAdded, "Templated strings should be suffixed with `_TEMPLATE`"))
		return issueList