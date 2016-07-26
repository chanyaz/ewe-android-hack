import Clauses
from Clauses.StringShouldNotHavePositionalPlaceholders import *
from Clauses.StringShouldNotHaveBrandSpecificTerm import *
from Clauses.TemplatedStringShouldBeSuffixedWithTEMPLATE import *
from Clauses.PlaceholderStringMustHaveSomeExamples import *
from Clauses.FeatureConfigurationChangesMayBeRequiredInAllBrands import *
from Clauses.RxPitfallCheck import *
from PullRequest import PullRequest
from IClause import *

stringShouldNotHavePositionalPlaceholders = StringShouldNotHavePositionalPlaceholders()
stringShouldNotHaveBrandSpecificTerm = StringShouldNotHaveBrandSpecificTerm()
templatedStringShouldBeSuffixedWithTEMPLATE = TemplatedStringShouldBeSuffixedWithTEMPLATE()
placeholderStringsShouldHaveSomeExamples = PlaceholderStringMustHaveSomeExamples()
featureConfigurationChangesMayBeRequiredInAllBrands = FeatureConfigurationChangesMayBeRequiredInAllBrands()
rxPitFallCheck = RxPitfallCheck()

ClausesList = [stringShouldNotHavePositionalPlaceholders, stringShouldNotHaveBrandSpecificTerm, templatedStringShouldBeSuffixedWithTEMPLATE, placeholderStringsShouldHaveSomeExamples, featureConfigurationChangesMayBeRequiredInAllBrands, rxPitFallCheck]

class ClauseProcessor:
        def runClauses(self, pullRequest):
			issues = []
			for clause in ClausesList:
				if clause.getType() == ClauseType.prLevel:
					issues.extend(clause.probableIssues(pullRequest))
				else:
					for file in pullRequest.files:
						issues.extend(clause.probableIssues(file))
			return issues

        def anyFileRelevantForAnyClause(self, filepathList):
			for filepath in filepathList:
				for clause in ClausesList:
					if clause.wantsToScanFile(filepath):
						return True
			return False
