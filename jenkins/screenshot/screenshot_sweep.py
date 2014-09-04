import string
import os
import sys
import adbtestutility

pathToApk = sys.argv[1]
screenshotDir = sys.argv[2]
testPackage = sys.argv[3]

# Methods
def getLanguageCode(combo):
	return combo[:string.index(combo, '_')]

def getCountryCode(combo):
	return combo[string.index(combo, '_')+1:len(combo)]

def makeDirectories(locCode):
	screenshotDir = adbtestutility.getPathToScreenshotDir() + '/'
	os.system('mkdir ' + screenshotDir)
	os.system('mkdir ' + screenshotDir + locCode)

def sendChangeLocaleCommand(language, country):
	adbLocaleCommand = 'adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X shell \"su -c \'setprop persist.sys.language {lang}; setprop persist.sys.country {co}; stop; sleep 5; start\'\"'.format(lang=language, co=country)
	print adbLocaleCommand
	os.system(adbLocaleCommand)

locs = {
   'da_dk',
   'de_at',
   'de_de',
   'en_au',
   'en_ca',
   'en_hk',
   'en_ie',
   'en_in',
   'en_nz',
   'en_sg',
   'en_uk',
   'en_us',
   'es_ar',
   'es_es',
   'es_mx',
   'fr_be',
   'fr_ca',
   'fr_fr',
   'id_id',
   'it_it',
   'ja_jp',
   'ko_kr',
   'ms_my',
   'nb_no',
   'nl_be',
   'nl_nl',
   'pt_br',
   'pt_pt',
   'sv_se',
   'th_th',
   'zh_HK',
   'zh_TW',
}

for locCode in locs:
	# Set up
	language = getLanguageCode(locCode)
	country = getCountryCode(locCode)
	makeDirectories(locCode)

	# Change device locale and restart it
	sendChangeLocaleCommand(language, country)

	apk = pathToApk + 'project-ExpediaAutomation-debug-unaligned.apk'
	testApk = pathToApk + 'project-ExpediaAutomation-debug-test-unaligned.apk'

	os.system('sleep 20')
	os.system('adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X uninstall com.expedia.bookings.auto.debug')
	os.system('adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X install {apkPath}'.format(apkPath=apk))

	# Run screenshot sweep
	adbtestutility.sendTestCommand(testPackage, apk, testApk)

	# Grab hotels screens and clean up
	adbtestutility.pullScreenshots(adbtestutility.getPathToScreenshotDir() + '/' + locCode + '/')

