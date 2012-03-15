# create change notes
SHA_VALUE=`git log -n 1 --oneline | awk '{print \$1}'`
OLD_SHA_VALUE=`tail -n 1 ~/VersionHistory/${JOB_NAME}.txt 2>/dev/null | awk '{print \$2}'`

if [ "$OLD_SHA_VALUE" ]; then
	if [ "$OLD_SHA_VALUE" != "$SHA_VALUE" ]; then
		MSG=`git log --pretty=format:"%an - %s" HEAD...$OLD_SHA_VALUE`
	else
		MSG="No changes since last successful build"
	fi
else
	MSG=`git log --pretty=format:"%an - %s" HEAD^..HEAD`
fi

# post build upload to HockeyApp
curl \
-F "status=2" \
-F "notify=1" \
-F "notes=$MSG" \
-F "notes_type=0" \
-F "ipa=@project/bin/ExpediaBookings-release.apk" \
-H "X-HockeyAppToken: bf8e54b34dcb40c4bee1364a75be509f" \
https://rink.hockeyapp.net/api/2/apps/4d9aae3faac40c74443772c8bebd5aaf/app_versions
