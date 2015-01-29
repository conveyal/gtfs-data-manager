var _ = require('underscore');
var Backbone = require('Backbone');

module.exports = Backbone.Model.extend({
    defaults: {
        name: null,
        isPublic: false,
        deployable: false,
        retrievalMethod: null,
        lastFetched: null,
        lastUpdated: null,
        url: null,
        latest: null,
        feedCollection: null
    },
    urlRoot: 'api/feedsources/'
});
