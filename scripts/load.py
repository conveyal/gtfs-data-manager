#!/usr/bin/python
# load the database to a fresh server
# usage: load.py dump.json http://localhost:9000

from sys import argv
import urllib2

server = argv[2]
# strip trailing slash to normalize url
server = server if not server.endswith('/') else server[:-1]

# TODO: don't load everything into RAM when loading
inf = open(argv[1])
dump = inf.read()

print dump[0:79]

req = urllib2.Request(server + '/load', dump, {'Content-Type': 'application/json', 'Content-Length': len(dump)})
opener = urllib2.build_opener()
opener.open(req)
