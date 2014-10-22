#!/usr/bin/python
# Dump the database from a server
# usage: dump.py http://localhost:9000 dump.json

from sys import argv
from shutil import copyfileobj
from getpass import getpass
from cookielib import CookieJar
import urllib2
from urllib import urlencode


server = argv[1]

# log in to the server
print 'Please authenticate'
uname = raw_input('username: ')
pw = getpass('password: ')

# strip trailing slash to normalize url
server = server if not server.endswith('/') else server[:-1]

# cookie handling
# http://www.techchorus.net/using-cookie-jar-urllib2
cj = CookieJar()
opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(cj))

# authenticate
opener.open(server + '/authenticate', urlencode(dict(username=uname, password=pw)))

# get the dump
out = open(argv[2], 'w')

res = opener.open(server + '/dump')
copyfileobj(res, out)
