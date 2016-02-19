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
				print >> file, "Group : " + dependency['group']
				print >> file, "Name : " + dependency['name']
				print >> file, "Current Version : " + dependency['version']
				print >> file, "Available Version : " + dependency['available']['release']
				print >> file, "-------------------------------------------"
			print "File created successfully."
		except Exception, e:
			print "Something went wrong. Check script"
			sys.exit(1)

main()
	