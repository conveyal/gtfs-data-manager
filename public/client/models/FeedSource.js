var _ = require('underscore');
var Backbone = require('Backbone');

module.exports.FeedSource = Backbone.Model.extend({
    defaults: {
        name: null,
        isPublic: null,
        autofetch: null,
        lastFetched: null,
        lastUpdated: null,
        fetchFrequency: null,
        url: null,
        latest: null,
        feedCollection: null
    },
    urlRoot: 'api/feedsources/'
});

module.exports.FeedSourceCollection = Backbone.Collection.extend({
    model: module.exports.FeedSource,
    url: 'api/feedsources',
    comparator: 'name'
});
