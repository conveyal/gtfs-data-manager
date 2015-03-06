var BB = require('bb');
var FeedSource = require('feed-source');
var _ = require('underscore');

module.exports = BB.Collection.extend({
  model: FeedSource,
  url: 'api/feedsources',

  /**
   * Get a function to compare two feed sources by the specified attribute.
   */
  comparator: function(fs1, fs2) {
    var attr = this.sortAttribute;

    var order = this.sortBackwards ? -1 : 1;

    var n1, n2;
    if (attr.indexOf('latestValidation.') === 0) {
      attr = attr.replace('latestValidation.', '');

      var lv1 = fs1.get('latestValidation');
      var lv2 = fs2.get('latestValidation');

      n1 = _.isUndefined(lv1) || _.isNull(lv1) ? null : lv1[attr];
      n2 = _.isUndefined(lv2) || _.isNull(lv2) ? null : lv2[attr];
    } else {

      n1 = fs1.get(attr);
      n2 = fs2.get(attr);
    }

    if (n1 == n2)
      return 0;

    if (fs1.id == fs2.id)
      return 0;

    // sort the new ones at the top, regardless of sort order
    if (_.isUndefined(fs1.id) || _.isNull(fs1.id))
      return -1;

    if (_.isUndefined(fs2.id) || _.isNull(fs2.id))
      return 1;

    if (_.isUndefined(n1) || _.isNull(n1))
      return order * -1;

    if (_.isUndefined(n2) || _.isNull(n2))
      return order * 1;

    if (n1 < n2)
      return order * -1;

    else
      return order * 1;
  },

  sortAttribute: 'name',
  sortBackwards: false
});
