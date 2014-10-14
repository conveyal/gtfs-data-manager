var _ = require('underscore');
var Backbone = require('Backbone');

module.exports.FeedCollection = Backbone.Model.extend({
    defaults: {
        name: null,
        id: null,
        user: null
    },
    urlRoot: 'api/feedcollections/'
});

// brought to you by your local department of redundancy department
module.exports.FeedCollectionCollection = Backbone.Collection.extend({
    model: module.exports.FeedCollection,
    url: 'api/feedcollections'
});
