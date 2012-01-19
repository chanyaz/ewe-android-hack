# This is a dumb script that reads a JSON response from Expedia's servers
# and spits out text that you can just insert into Java code.  Useful
# for debugging certain responses.

import sys

f = open(sys.argv[1], 'r')
contents = f.read()
print contents.replace("\/", "/").replace('"', '\\"')
