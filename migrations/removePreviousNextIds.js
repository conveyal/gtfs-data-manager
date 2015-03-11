// We used to store feed versions as a linked list, but the updates got messy, so now we use an index on version.

var fs = require('fs');

var dump = JSON.parse(fs.readFileSync(process.argv[process.argv.length - 2]));

dump.feedVersions.forEach(function (fv) {
  fv.previousVersionId = fv.nextVersionId = undefined;
});

fs.writeFileSync(process.argv[process.argv.length - 1], JSON.stringify(dump));
