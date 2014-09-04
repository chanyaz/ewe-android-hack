import sys
import json

key = sys.argv[1]
val = sys.argv[2]

f = open("config.json", "r+")
json_dict = json.load(f)

json_dict[key] = val
write_text = json.dumps(json_dict, sort_keys=True, indent=4)

f.seek(0)
f.truncate()
f.write(write_text)
f.close()

print 'Added key:value pair ' + key + ':' + val
