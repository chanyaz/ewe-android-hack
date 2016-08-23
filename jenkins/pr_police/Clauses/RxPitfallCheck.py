from IClause import *
import File
from Line import *
from Issue import *
import re
from SourceCodeValidationClause import *

class RxPitfallCheck(SourceCodeValidationClause):

    def unsafeRxFunctions(self):
        return [r'textChanges', r'textChangeEvents', r'beforeTextChangeEvents', r'afterTextChangeEvents']

    def probableIssuesOnLineInFile(self, file, line, index):
        issueList = []
        if any(re.search(pattern, line.codeLineAdded) for pattern in self.unsafeRxFunctions()):
            issueList.append(Issue(file.commitId, file.filename, line.codeLineNumberInFile, line.linePositionInDiff, line.codeLineAdded, "Before using any of the functions: textChanges(), textChangeEvents(), beforeTextChangeEvents() or afterTextChangeEvents() please refer to the PR: [ https://github.com/ExpediaInc/ewe-android-eb/pull/5418 ]  as these functions introduce mutable state and could lead to issues that are hard to debug.", IssueType.warning))
        return issueList