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

fs.writeFileSync(process.argv[process.argv.length - 1], JSON.stringify(dump));
