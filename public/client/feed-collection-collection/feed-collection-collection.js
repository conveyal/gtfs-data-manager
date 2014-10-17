// brought to you by your local department of redundancy department
var _ = require('underscore');
var Backbone = require('Backbone');
var FeedCollection = require('feed-collection');

module.exports = Backbone.Collection.extend({
    model: FeedCollection,
    url: 'api/feedcollections',
    comparator: 'name'
});
