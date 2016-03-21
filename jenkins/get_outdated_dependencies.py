#!/usr/bin/python
# This will generate a file listing outdated dependencies found by dependencyUpdates gradle task.
import json
import sys

def main():
	dependencyUpdatesJsonFile = open('./build/dependencyUpdates/report.json').read()
	dependencyUpdatesJson  = json.loads(dependencyUpdatesJsonFile)
	try:
		outdatedDependencies = dependencyUpdatesJson["outdated"]
	except Exception, e:
		print "Everything upto date."
	else:
		try:
			file = open ('outdated_dependecies.txt', 'w')
			for dependency in outdatedDependencies['dependencies'] :
				str = "%s: %s - %s -> %s" % (dependency['group'], dependency['name'], dependency['version'], dependency['available']['release'])
				print >> file, str
			print "File created successfully."
		except Exception, e:
			print "Something went wrong. Check script"
			sys.exit(1)
main()
	
