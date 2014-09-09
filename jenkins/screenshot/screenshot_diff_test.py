import os
import sys
import errno
import os.path
import glob
import adbtestutility

pathToApk = sys.argv[1]
screenshotDir = sys.argv[2]
#threshold in % of (different pixels/total pixels)
threshold = sys.argv[3]
oldDir=adbtestutility.getPathToScreenshotDir() + '/OldScreenShots'
dir=adbtestutility.getPathToScreenshotDir() + '/' + 'NewScreenShots'

# Methods
def makeDirectories(name):
	screenshotDir = adbtestutility.getPathToScreenshotDir() + '/'+name
	hotelsTarget = screenshotDir  + '/hotels'
	flightsTarget = screenshotDir  + '/flights'
	os.system('mkdir ' + screenshotDir)
	os.system('mkdir ' + hotelsTarget)
	os.system('mkdir ' + flightsTarget)

def makeOldFolderDir():
	os.system('rm -rf '+ oldDir)
	os.system('mkdir ' + oldDir)

def imageCompare(var):
	for (newF,oldF) in zip((glob.glob(dir+"/"+var+"/*.jpg")),(glob.glob(oldDir+"/"+var+"/*.jpg"))):
		newF=newF.replace(" ","\ ")
		oldF=oldF.replace(" ","\ ")
		resultName=newF.split("\ ")[1]
		print newF + " , "
		print oldF + " "
		# imageDiff php script call ,arguments: <new image>,<old image>, <result image name> , <threshold value>
		os.system('php image_diff.php '+ newF+ " "+ oldF+' '+var+'_diff_'+resultName+'.jpg '+threshold)

def deleteimagefiles(dirname):
	filelist=glob.glob(dirname)
	for f in filelist:
		os.remove(f)

# Set up
makeDirectories('NewScreenShots')
os.system('mkdir '+ adbtestutility.getPathToScreenshotDir() +'/ImageDiffResults')

# Delete .jpg files from previous test run
deleteimagefiles(adbtestutility.getPathToScreenshotDir()+"/ImageDiffResults/*.jpg")
deleteimagefiles(adbtestutility.getPathToScreenshotDir()+"/*.jpg")

# Run hotels screenshot sweep
adbtestutility.sendTestCommand('com.expedia.bookings.test.tests.localization.HotelImageDiff')

# Grab hotels screens and clean up
try:
    os.rmdir(dir + '/hotels')
except OSError as ex:
    if ex.errno == errno.ENOTEMPTY:
        makeOldFolderDir();
        os.system('cp -r ' +dir+'/* '+ oldDir)
        os.system('rm '+dir +'/hotels/*')

adbtestutility.pullScreenshots(dir + '/hotels')
adbtestutility.deleteScreenshots()

# Run flights screenshot sweep
adbtestutility.sendTestCommand('com.expedia.bookings.test.tests.localization.FlightsImageDiff')

# Grab flights screens and clean up
try:
    os.rmdir(dir + '/flights')
except OSError as ex:
    if ex.errno == errno.ENOTEMPTY:
        os.system('rm '+dir +'/flights/*')

adbtestutility.pullScreenshots(dir + '/flights')
adbtestutility.deleteScreenshots()

#Read image files and perform imagediff
try:
	imageCompare('hotels');
except:
	print "Fail"

try:
	imageCompare('flights');
except:
	print "Fail"

os.system("mv "+ adbtestutility.getPathToScreenshotDir()+"/*.jpg "+ adbtestutility.getPathToScreenshotDir()+"/ImageDiffResults/")

# Check if image-diff files are created,if yes->test fails
for file in os.listdir(adbtestutility.getPathToScreenshotDir()+"/ImageDiffResults/"):
	if file.endswith(".jpg"):
		print "FAIL"
		sys.exit(1)

