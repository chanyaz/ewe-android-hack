from abc import *	

class IClause(object):
	__metaclass__ = ABCMeta

	def __init__(self):
		pass

	@abstractmethod
	def wantsToScanFile(self, filepath):
		"check whether the clause wants to scan a particular file"
		return

	@abstractmethod
	def probableIssues(self, file):
		"check whether the clause is applicable on the particular pull request"
		return