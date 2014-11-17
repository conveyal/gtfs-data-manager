// Add the GTFS Data Manager ID to a dump, extracting the ID from the existing file

var fs = require('fs');

var dump = JSON.parse(fs.readFileSync(process.argv[process.argv.length - 2]));

dump.feedSources.forEach(function (source) {
  if (source.retrievalMethod == 'PRODUCED_IN_HOUSE' && source.url !== null) {
    var id = /agencySelect=([0-9]+)/.exec(source.url)[1];
    id = Number(id);
    source.url = null;
    source.editorId = id;
  } else {
    source.editorId = null;
  }
});

// bounds should not be included in deployments, since they are calculated on the fly
// but there was a bug before this migration was created that caused them to be, and then the files would fail
// reserialization. So we drop the field
dump.deployments.forEach(function (d) {
  if (d.bounds !== undefined)
    delete d.bounds;
});

fs.writeFileSync(process.argv[process.argv.length - 1], JSON.stringify(dump));
