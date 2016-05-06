from abc import *
from enum import Enum

class ClauseType(Enum):
	# When we want to apply the clause as file level
	fileLevel = 1
	# When we want to apply the clause at PR level
	prLevel = 2


class IClause(object):
	__metaclass__ = ABCMeta

	def __init__(self):
		self.type = ClauseType.fileLevel
		pass

	def getType(self):
		return self.type

	@abstractmethod
	def wantsToScanFile(self, filepath):
		"check whether the clause wants to scan a particular file"
		return

	@abstractmethod
	def probableIssues(self, file):
		"check whether the clause is applicable on the particular pull request"
		return