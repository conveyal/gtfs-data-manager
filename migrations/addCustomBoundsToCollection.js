/**
 * Default feed collections to not using custom bounds
 */

 var fs = require('fs');

 var dump = JSON.parse(fs.readFileSync(process.argv[process.argv.length - 2]));

dump.feedCollections.forEach(function (fc) {
  fc.useCustomOsmBounds = false;
});

 fs.writeFileSync(process.argv[process.argv.length - 1], JSON.stringify(dump));
