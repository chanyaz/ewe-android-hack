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

def changeLocale(language, country):
    setLanguageCommand = 'python add_key_value_pair_to_config.py language {lang}'.format(lang=language)
    os.system(setLanguageCommand)
    print setLanguageCommand
    
    setCountryCommand = 'python add_key_value_pair_to_config.py country {co}'.format(co=country)
    os.system(setCountryCommand)
    print setCountryCommand
    
    pushConfigCommand = 'adb push config.json /sdcard/config.json'
    os.system(pushConfigCommand)
    print pushConfigCommand

locs = {
    'da_DK',
    'de_AT',
    'de_CH',
    'de_DE',
    'en_AU',
    'en_CA',
    'en_GB',
    'en_HK',
    'en_KR',
    'en_IE',
    'en_IN',
    'en_ID',
    'en_JP',
    'en_MY',
    'en_NZ',
    'en_PH',
    'en_SG',
    'en_UK',
    'en_TH',
    'en_TW',
    'en_US',
    'es_AR',
    'es_ES',
    'es_MX',
    'es_US',
    'fi_FI',
    'fr_BE',
    'fr_CA',
    'fr_CH',
    'fr_FR',
    'id_ID',
    'it_IT',
    'ja_JP',
    'ko_KR',
    'ms_MY',
    'nb_NO',
    'nl_BE',
    'nl_NL',
    'pt_BR',
    'sv_SE',
    'th_TH',
    'vi_VN',
    'zh_CN',
    'zh_HK',
    'zh_TW',
    'zh_US'
}

apk = pathToApk + 'project-expedia-debug-unaligned.apk'
testApk = pathToApk + 'project-expedia-debug-androidTest-unaligned.apk'

os.system('adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X uninstall com.expedia.bookings.latest')
os.system('adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X install {apkPath}'.format(apkPath=apk))

for locCode in locs:
	# Set up
	language = getLanguageCode(locCode)
	country = getCountryCode(locCode)
	makeDirectories(locCode)
    
	# Change device locale
	changeLocale(language, country)
    
	# Run screenshot sweep
	adbtestutility.sendTestCommand(testPackage, apk, testApk)
    
	# Grab screenshots and spoon index.html
	adbtestutility.pullScreenshots(adbtestutility.getPathToScreenshotDir() + '/' + locCode + '/')




