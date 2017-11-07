import os
import sys

targetName = sys.argv[1]
brandName = sys.argv[2]
releaseVersion = sys.argv[3]
iteration = sys.argv[4]

def getBuildSize(brandName):
	return round(os.stat('project/build/outputs/apk/{target}/release/project-{brand}-release-{releaseVersion}_RC{iteration}.apk'
						 .format(target=targetName, brand=brandName, releaseVersion=releaseVersion, iteration=iteration)).st_size / float(1024*1024), 2)

def saveBuildSizeToFile(buildSize):
	release_apk_size_file = open("releaseApkSize.txt", "w")
	release_apk_size_file.write(str(buildSize))
	release_apk_size_file.close()

try:
	saveBuildSizeToFile(getBuildSize(brandName))
except Exception as e:
	print "Exception encountered while trying to fetch latest Release APK size"
