var _ = require('underscore');
var Backbone = require('Backbone');
var FeedVersion = require('feed-version');

module.exports = Backbone.Collection.extend({
    model: FeedVersion,
    url: 'api/feedversions',
    // reverse-sort by version
    comparator: function (model) {
        return -model.get('version');
    }
});
