var _ = require('underscore');
var Backbone = require('Backbone');
var FeedSource = require('feed-source');

module.exports = Backbone.Collection.extend({
  model: FeedSource,
  url: 'api/feedsources',

  /**
   * Get a function to compare two feed sources by the specified attribute.
   */
  compareBy: function(attr) {
    return function(fs1, fs2) {
      var n1 = fs1.get(attr);
      var n2 = fs2.get(attr);

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
  },

  /**
   * sort by a specified attribute of a feed source.
   * new feed sources (that haven't been saved) are sorted first.
   */
  sortBy: function(attr) {
    return Backbone.Collection.prototype.sortBy.call(this, this.compareBy(attr));
  }
});
