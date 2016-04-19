import Clauses
from Clauses.StringShouldNotHavePositionalPlaceholders import *
from Clauses.StringShouldNotHaveBrandSpecificTerm import *
from Clauses.TemplatedStringShouldBeSuffixedWithTEMPLATE import *
from Clauses.PlaceholderStringMustHaveSomeExamples import *
from PullRequest import PullRequest

stringShouldNotHavePositionalPlaceholders = StringShouldNotHavePositionalPlaceholders()
stringShouldNotHaveBrandSpecificTerm = StringShouldNotHaveBrandSpecificTerm()
templatedStringShouldBeSuffixedWithTEMPLATE = TemplatedStringShouldBeSuffixedWithTEMPLATE()
placeholderStringsShouldHaveSomeExamples = PlaceholderStringMustHaveSomeExamples()

ClausesList = [stringShouldNotHavePositionalPlaceholders, stringShouldNotHaveBrandSpecificTerm, templatedStringShouldBeSuffixedWithTEMPLATE, placeholderStringsShouldHaveSomeExamples]

class ClauseProcessor:
        def runClauses(self, pullRequest):
			issues = []
			for file in pullRequest.files:
				for clause in ClausesList:
					issues.extend(clause.probableIssues(file))
			return issues

        def anyFileRelevantForAnyClause(self, filepathList):
			for filepath in filepathList:
				for clause in ClausesList:
					if clause.wantsToScanFile(filepath):
						return True
			return False
