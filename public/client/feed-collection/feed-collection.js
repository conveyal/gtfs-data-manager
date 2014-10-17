var _ = require('underscore');
var Backbone = require('Backbone');

module.exports = Backbone.Model.extend({
    defaults: {
        name: null,
        id: null,
        user: null
    },
    urlRoot: 'api/feedcollections/'
});
