# This script tests whether all the suggest destinations
# that we use on the launch page are good and accurate from
# the suggest API.

import urllib
import json
import re

# From https://confluence/display/POS/Search+and+Disambig+Optimization#SearchandDisambigOptimization-ExpediaSuggestSupportedLocales
locales = [
"en_TH",
	"en_AU",
	"en_HK",
	"zh_HK",
	"en_IN",
	"id_ID",
	"en_AU",
	"ja_JP",
	"en_US",
	"ko_KR",
	"en_MY",
	"ms_MY",
	"en_NZ",
	"en_PH",
	"tl_PH",
	"en_SG",
	"zh_TW",
	"en_AU",
	"th_TH",
	"vi_VN",
	"de_AT",
	"fr_BE",
	"nl_BE",
	"da_DK",
	"fr_FR",
	"de_DE",
	"en_IE",
	"it_IT",
	"nl_NL",
	"nb_NO",
	"es_ES",
	"sv_SE",
	"es_AR",
	"pt_BR",
	"en_CA",
	"fr_CA",
	"es_MX",
]

# From the app
destinations = [
	"SEA",
	"SFO",
	"LON",
	"PAR",
	"LAS",
	"NYC",
	"YYZ",
	"HKG",
	"MIA",
	"BKK",
]

pattern = re.compile("^(.+)\\((.+)\\)$")

for locale in locales:
	for destination in destinations:
		url = "http://suggest.expedia.com/hint/es/v2/ac/%s/%s?type=95&lob=Flights" % (locale, destination)
		print url
		response = urllib.urlopen(url)
		content = response.read()
		data = json.loads(content[1:-1])
		target = data["sr"][0]["l"]
		print "%s (%s): %s" % (destination, locale, target)
		if pattern.match(target) is None:
			print "!!!!! DOES NOT MATCH !!!!!"
		
