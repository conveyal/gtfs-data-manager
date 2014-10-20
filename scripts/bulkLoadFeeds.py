#!/usr/bin/python
# Bulk load a bunch of GTFS feeds to the GTFS data manager

import csv
from getpass import getpass
from sys import argv
import json
from cookielib import CookieJar
import urllib2
from urllib import urlencode
from zipfile import ZipFile
# multipart file upload
from poster.encode import multipart_encode
from poster.streaminghttp import register_openers

if len(argv) < 3:
    print 'usage: %s feed.zip [feed2.zip feed3.zip . . .] http://gtfs-data-manager.example.com' % argv[0]

server = argv[-1]

# log in to the server
print 'Please authenticate'
uname = raw_input('username: ')
pw = getpass('password: ')

# strip trailing slash to normalize url
server = server if not server.endswith('/') else server[:-1]

# cookie handling
# http://www.techchorus.net/using-cookie-jar-urllib2
# and http://stackoverflow.com/questions/1690446
cj = CookieJar()
opener = register_openers()
opener.add_handler(urllib2.HTTPCookieProcessor(cj))

# authenticate
opener.open(server + '/authenticate', urlencode(dict(username=uname, password=pw)))

# choose feed collection
colls = json.load(opener.open(server + '/api/feedcollections'))

print 'choose a feed collection: '

for i in xrange(len(colls)):
    print '%s. %s' % (i + 1, colls[i]['name'])
    
while True:
    try:
        coll = colls[int(raw_input('> ')) - 1]
    except ValueError:
        continue
    else:
        break

# load each feed

for feed in argv[1:-1]:
    print 'processing feed %s' % feed
    
    # figure out what to call it, by looking at agency.txt
    reader = csv.DictReader(ZipFile(feed).open('agency.txt'))
    name = reader.next()['agency_name']

    print 'detected agency name %s' % name

    # create a feedsource
    fs = dict(
        name = name,
        url = None,
        isPublic = True,
        autofetch = False,
        feedCollection = coll,
    )

    req = urllib2.Request(server + '/api/feedsources/', json.dumps(fs), {'Content-Type': 'application/json'})
    res = opener.open(req)
    
    fs = json.loads(res.read())

    print 'Created feed source with ID %s' % fs['id']

    # upload the feed
    data, head = multipart_encode(dict(feedSourceId=fs['id'], feed=open(feed, 'rb')))
    req = urllib2.Request(server + '/api/feedversions/', data, head)
    opener.open(req)

    print 'done'
                                  
    
    
