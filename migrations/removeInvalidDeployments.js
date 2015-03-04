/*
 * There was a bug where deployments could be created from invalid versions. Delete these deployments.
 */

var fs = require('fs');

var dump = JSON.parse(fs.readFileSync(process.argv[process.argv.length - 2]));

var before = dump.deployments.length;

dump.deployments = dump.deployments.filter(function (deployment) {
   return deployment.feedVersionIds.indexOf(null) === -1;
 });

var after = dump.deployments.length;

console.log("Removed " + (before - after) + " invalid deployments");

fs.writeFileSync(process.argv[process.argv.length - 1], JSON.stringify(dump));
