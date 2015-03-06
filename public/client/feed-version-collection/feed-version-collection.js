var BB = require('bb');
var FeedVersion = require('feed-version');

module.exports = BB.Collection.extend({
  model: FeedVersion,
  url: 'api/feedversions',
  // sort by name, then reverse-sort by version
  comparator: function(model0, model1) {
    if (model0.get('feedSource').name < model1.get('feedSource').name)
      return -1;
    else if (model0.get('feedSource').name > model1.get('feedSource').name)
      return 1;

    // names are the same, sort by version
    return model1.get('version') - model0.get('version');
  }
});
