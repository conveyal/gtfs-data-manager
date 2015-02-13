# GTFS Data Manager

This is a workflow tool for managing large amounts of GTFS data. It will allow users to manage feeds coming in from many agencies, see if they are valid and can be loaded, see and manage what feeds are about to expire, and deploy feeds to staging and production OpenTripPlanner servers.

It will eventually be integrated with [https://github.com/conveyal/gtfs-editor](GTFS Editor) to provide a complete GTFS workflow tool.

## Installation

It's a Play! 2.3.x app, so installation should be fairly simple:

1. Clone the git repository.
1. Edit application.conf, changing the application secrets and the GTFS and MapDB directories (these directories must already exist).
1. cd into the `public/` directory and run `component build -dc`
   (-d runs the build in 'development mode,' which creates the short aliases to the libraries that we use. -c copies assets instead of linking them, which is important for portable builds, and also on operating systems that don't properly support links).
1. type `./activator run` to install dependencies and start the app.
1. Create the first user by hitting a URL with curl (this will eventually be made prettier): `curl -D - -X POST --data 'username=<username>&password=<password>&email=<email>&admin=true' http://<server>:9000/createInitialUser` (this will only work once; after that you can log in as admin and create or manage other accounts, including creating non-admin users with access only to specific feeds).
1. Have at it!

If you're running it on a Linux/OS X server which has multiple user accounts running GeoTools application, you may need to use a non-default temporary directory, e.g.:

    TMPDIR=`mktemp -d` ./activator run

See [this blog post](http://www.indicatrix.org/2014/10/20/using-geotools-with-multiple-user-accounts/) for details.

## Deployment

One of the key features of Data Manager is the ability to deploy to and manage [OpenTripPlanner](http://opentripplanner.org)
servers. They are configured in conf/application.conf. It works by pulling down OSM corresponding to the area of the
GTFS feeds from a [vex](https://github.com/conveyal/vanilla-extract.git) server and then making zip of all the feeds and
the OSM, which is fed to an OTP server.

## Sharing feeds

Any admin can see a share link for each feed. Sharing this link will allow others to log in and retrieve or deploy only that feed.

## Integration with [gtfs-editor](https://github.com/conveyal/gtfs-editor)

It is possible, and encouraged, to connect Data Manager with GTFS Editor. This is done via OAuth 2; configure an OAuth 2
client ID and client secret in both the GTFS Editor and Data Manager configuration files (they must match!) and input the URL to
editor in the Data Manager config file.

Two things are now possible:

1. You can set a feed's retrieval method to "Produced in house" and choose a GTFS Editor agency to link it to.
1. Any logged-in user can edit feeds they have permission to deploy or update by clicking the edit button in Manager;
   they will be sent to Editor, where they will be able to edit the feed.

This closes the loop on editing to production: any user can edit their feed, pull it into Data Manager and then deploy
it to an [OpenTripPlanner](http://opentripplanner.org) server to see how it works, iterating as necessary. Lather, rinse,
repeat. The GTFS production loop can now be made much tighter.

## Loading feeds

Feeds can be loaded directly through the admin interface, but if you have a lot of them this can be a pain. We provide two tools to load feeds in bulk. Both require that a new project be created through the admin interface.

### `scripts/bulkLoadUrls.py`

Loads URLs from a CSV with `name` and `url`  fields. Use it like so

    bulkLoadUrls.py file.csv <server>

where <server> is the URL of your GTFS data manager instance (most likely http://localhost:9000).

### `scripts/bulkLoadFeeds.py`

Loads a bunch of feeds. Use it like so

    bulkLoadFeeds.py feed.zip [feed2.zip feed3.zip . . .] <server>
