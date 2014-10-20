# GTFS Data Manager

This is a workflow tool for managing large amounts of GTFS data. It will allow users to manage feeds coming in from many agencies, see if they are valid and can be loaded, see and manage what feeds are about to expire, and deploy feeds to staging and production OpenTripPlanner servers.

It will eventually be integrated with [https://github.com/conveyal/gtfs-editor](GTFS Editor) to provide a complete GTFS workflow tool.

## Installation

It's a Play! 2.3.x app, so installation should be fairly simple:

1. Clone the git repository.
1. Edit application.conf, changing the application secrets and the GTFS and MapDB directories (these directories must already exist).
1. cd into the `public/` directory and run `component build`
1. type `./activator run` to install dependencies and start the app.
1. Create the first user by hitting a URL with curl (this will eventually be made prettier): `curl -D - -X POST --data 'username=<username>&password=<password>&email=<email>&admin=true' http://<server>:9000/createInitialUser` (this will only work once)
1. Have at it!

If you're running it on a Linux/OS X server which has multiple user accounts running GeoTools application, you may need to use a non-default temporary directory, e.g.:

    TMPDIR=`mktemp -d` ./activator run

See [this blog post](http://www.indicatrix.org/2014/10/20/using-geotools-with-multiple-user-accounts/) for details.

## Loading feeds

Feeds can be loaded directly through the admin interface, but if you have a lot of them this can be a pain. We provide two tools to load feeds in bulk. Both require that a new project be created through the admin interface.

### `scripts/bulkLoadUrls.py`

Loads URLs from a CSV with `name` and `url`  fields. Use it like so

    bulkLoadUrls.py file.csv <server>

where <server> is the URL of your GTFS data manager instance (most likely http://localhost:9000).

### `scripts/bulkLoadFeeds.py`

Loads a bunch of feeds. Use it like so
  
    bulkLoadFeeds.py feed.zip [feed2.zip feed3.zip . . .] <server>