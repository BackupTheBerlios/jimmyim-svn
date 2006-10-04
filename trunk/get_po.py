#!/usr/bin/env python

import sys
import re

input = sys.stdin.read()
input = input.split("\n")

for line in input[:-1]:
    l = line.split(":")
    print "#: %s:%s" % (l[0],l[1])

    multi = re.findall('Localization.tr\(\".+?\"\)', l[2])
    for m in multi:
	print "msgid %s" % re.findall('\".*\"', m)[0]
	print "msgstr \"\"\n"

    #print "msgid %s" % re.findall('\".*\"', l[2])[0]
    #print "msgstr \"\"\n"

