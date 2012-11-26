# Quick script for running adb pull to get screenshots off of Android device/emulator

LOCAL_DIRECTORY='screenshots'
ANDROID_DIRECTORY='sdcard/Robotium-Screenshots'

locales=(	"en_UK" 
		"fr_CA" 	
		"en_HK"
		"zh_HK" 
		"es_AR" 
		"en_AU"
		"de_AT" 
		"fr_BE" 
		"nl_BE"
		"pt_BR" 
		"en_CA" 
		"da_DK"
		"fr_FR" 
		"de_DE" 
		"en_IN"
		"id_ID" 
		"en_IE"
		"in_ID" 
		"it_IT"
		"ja_JP" 
		"es_MX" 
		"en_MY"
		"en_TW"
		"ms_MY"
		"nl_NL" 
		"en_NZ" 
		"nb_NO"
		"en_SG" 
		"en_PH" 
		"ko_KR"
		"es_ES" 
		"sv_SE" 
		"zh_TW"
		"en_US" 
		"th_TH" 
		"vi_VN"
		"tl_PH" 
		"zh_CN"
		)

echo "Moving files from $ANDROID_DIRECTORY TO $LOCAL_DIRECTORY"

if [ ! -d "$LOCAL_DIRECTORY" ]; then
    # Control will enter here if $DIRECTORY doesn't exist.
    mkdir $LOCAL_DIRECTORY
fi

adb shell ls | adb pull $ANDROID_DIRECTORY $LOCAL_DIRECTORY/ | rm -r "$ANDROID_DIRECTORY" |  exit

cd $LOCAL_DIRECTORY

for locale in "${locales[@]}"
   do

   #Checks how many files for a locale are existing
   NUM=$(ls "$locale"* | wc -l) 
   
   # If directory doesn't already exist and there are files in existence
   # make directory and move appropriate files to it	
   if [ ! -d "$locale" ] && [ "$NUM" -gt "0" ]; then
	
	mkdir $locale
   
	mv $locale*.jpg $locale   
  
   fi

   done
