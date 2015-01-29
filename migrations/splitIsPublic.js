/**
 * make isPublic the deployable tag, and create a new isPublic tag and initialize it to false.
 */

var fs = require('fs');

var dump = JSON.parse(fs.readFileSync(process.argv[process.argv.length - 2]));

dump.feedSources.forEach(function (feedSource) {
  feedSource.deployable = feedSource.isPublic;
  // default false
  feedSource.isPublic = false;
});

fs.writeFileSync(process.argv[process.argv.length - 1], JSON.stringify(dump));
