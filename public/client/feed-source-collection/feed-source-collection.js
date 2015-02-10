var _ = require('underscore');
var Backbone = require('Backbone');
var FeedSource = require('feed-source');

module.exports = Backbone.Collection.extend({
    model: FeedSource,
    url: 'api/feedsources',
    comparator: function (fs1, fs2) {
      var n1 = fs1.get('name');
      var n2 = fs2.get('name');

      if (n1 === n2)
        return 0;

      if (fs1.id === fs2.id)
        return 0;

      // sort the new ones at the top
      if (_.isUndefined(fs1.id) || _.isNull(fs1.id))
        return -1;

      if (_.isUndefined(fs2.id) || _.isNull(fs2.id))
        return 1;

      if (n1 < n2)
        return -1;

      else
        return 1;
    }
});
