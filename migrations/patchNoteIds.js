// Patch up the note IDs
// At one point, the dumper was not saving the reference from feed sources and feed version to notes, although
// the reverse was there. Reconstruct the lost data.

var fs = require('fs');
var dump = JSON.parse(fs.readFileSync(process.argv[process.argv.length - 2]));

// index feedsources and feedversions
var fsIdx = {};

dump.feedSources.forEach(function (fs, i) {
  fsIdx[fs.id] = i;
});

var fvIdx = {};

dump.feedVersions.forEach(function (fv, i) {
  fvIdx[fv.id] = i;
});

dump.notes.forEach(function (note) {
  var target;
  if (note.type == "FEED_SOURCE") {
    target = dump.feedSources[fsIdx[note.objectId]];
  } else if (note.type == "FEED_VERSION") {
    target = dump.feedVersions[fvIdx[note.objectId]];
  }
  // also types were not getting stored, so reconstruct that as well.
  // there is no chance of id collision as format is different.
  else if (fsIdx[note.objectId] !== undefined) {
    target = dump.feedSources[fsIdx[note.objectId]];
  } else if (fvIdx[note.objectId] !== undefined) {
    target = dump.feedVersions[fvIdx[note.objectId]];
  } else {
    console.log("Cannot find object to attach note to: ", note);
    return;
  }

  if (target.noteIds === undefined || target.noteIds === null)
    target.noteIds = [];

  target.noteIds.push(note.id);
});

fs.writeFileSync(process.argv[process.argv.length - 1], JSON.stringify(dump));
