import re
from Line import *

class PatchBlockMetaData:
	def __init__(self, patchLine):
		# Expect one of two formats:
		#   @@ -a,b +c,d @@
		#   @@ -a +c @@
		metaData = re.findall('\d+', patchLine)	
		self.lineNumberBeforeModification = int(metaData[0])
		if len(metaData) == 4:
			self.numberOfLinesInBlockBeforeModification = int(metaData[1])
			self.lineNumberAfterModification = int(metaData[2])
			self.numberOfLinesInBlockAfterModification = int(metaData[3])
		else:
			self.lineNumberAfterModification = int(metaData[1])
			self.numberOfLinesInBlockBeforeModification = 1
			self.numberOfLinesInBlockAfterModification = 1

class Patch:

	def __init__(self, patch):
		self.fileLines = []	
		self.patchLineIndex = 0
		self.linePositionInDiff = -1
		if not patch is None:
			patchLines = patch.split('\n')
			self.parsePatch(patchLines)

	def parsePatch(self, patchLines):
		for patchLine in patchLines:
			self.linePositionInDiff += 1
			self.checkAndHandleStartOfANewBlock(patchLine, patchLines)
			self.checkAndHandleAddedLine(patchLine, patchLines)
			self.checkAndHandleDeletedLine(patchLine)	
			self.patchLineIndex += 1
	
	def checkAndHandleStartOfANewBlock(self, patchLine, patchLines):
		if patchLine.startswith('@@'):
			#New block starts
			self.resetBlock(patchLine, patchLines)
						
	def resetBlock(self, patchLine, patchLines):
		self.patchBlockMetaData = PatchBlockMetaData(patchLine)			
		self.blockStartLineIndex = patchLines.index(patchLine)
		self.numDeletionsInBlock = 0
		self.numAdditionsInBlock = 0
		#Holds the index of last deleted line. To be compared with the index of an added line, for detecting 'modification'.
		#Note that this will be reset to 0 during that comparison. And will hold a non-zero value again when a deleted line is found.
		self.lastDeletedLineIndex = 0

	def checkAndHandleDeletedLine(self, patchLine):
		if patchLine.startswith('-'):
			self.lastDeletedLineIndex = self.codeLineNumberInFile = self.patchLineIndex - self.blockStartLineIndex + self.patchBlockMetaData.lineNumberBeforeModification - 1 - self.numAdditionsInBlock			
			self.numDeletionsInBlock += 1
			self.fileLines.append(Line('', patchLine[1:], LineOperation.deleted, self.codeLineNumberInFile, self.linePositionInDiff))

	def checkAndHandleAddedLine(self, patchLine, patchLines):
		if patchLine.startswith('+'):
			self.codeLineNumberInFile = self.patchLineIndex - self.blockStartLineIndex - 1 + self.patchBlockMetaData.lineNumberAfterModification - self.numDeletionsInBlock 
			#-1 accounts for the '@@' line at the start of this block
			self.oldCodeLineNumberInFileIfModified = self.patchLineIndex - self.blockStartLineIndex - 1 + self.patchBlockMetaData.lineNumberBeforeModification - self.numAdditionsInBlock - 1
			#-1 accounts for the deleted line in lieu of which this line has been added, if this indeed is a modification operation, which we detect below
			#Modification case
			if self.oldCodeLineNumberInFileIfModified == self.lastDeletedLineIndex:
				self.fileLines.append(Line(patchLine[1:], (patchLines[self.patchLineIndex - 1])[1:], LineOperation.added, self.codeLineNumberInFile, self.linePositionInDiff))
			else:
				#pure Addition
				self.fileLines.append(Line(patchLine[1:], '', LineOperation.added, self.codeLineNumberInFile, self.linePositionInDiff))
			self.numAdditionsInBlock += 1
			self.lastDeletedLineIndex = 0