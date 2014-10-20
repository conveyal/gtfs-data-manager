#!/usr/bin/python
# load many feeds to the GTFS data manager, from a csv with fields name and url
# usage: bulkLoadFeeds.py file.csv http://server.example.com/

import csv
from getpass import getpass
from sys import argv
import json
from cookielib import CookieJar
import urllib2
from urllib import urlencode

if len(argv) != 3:
    print 'usage: %s file.csv http://gtfs-data-manager.example.com' % argv[0]

server = argv[2]

with open(argv[1]) as f:
    reader = csv.DictReader(f)

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
    for feed in reader:
        data = dict(
            name = feed['name'],
            url = feed['url'],
            isPublic = True,
            autofetch = True,
            # every day
            feedCollection = coll
        )

        # http://stackoverflow.com/questions/3290522
        req = urllib2.Request(server + '/api/feedsources/', json.dumps(data), {'Content-Type': 'application/json'})
        opener.open(req)
    

    

        
