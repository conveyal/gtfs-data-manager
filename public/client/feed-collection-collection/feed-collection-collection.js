// brought to you by your local department of redundancy department
var BB = require('bb');
var FeedCollection = require('feed-collection');

module.exports = BB.Collection.extend({
  model: FeedCollection,
  url: 'api/feedcollections',
  comparator: 'name'
});
