var _ = require('underscore');
var Backbone = require('Backbone');
var FeedSource = require('feed-source');

module.exports = Backbone.Collection.extend({
    model: FeedSource,
    url: 'api/feedsources',
    comparator: 'name'
});
