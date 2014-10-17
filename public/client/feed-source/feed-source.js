var _ = require('underscore');
var Backbone = require('Backbone');

module.exports = Backbone.Model.extend({
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
