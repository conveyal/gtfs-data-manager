var BB = require('bb');

module.exports = BB.Model.extend({
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
