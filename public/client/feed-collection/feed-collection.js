var _ = require('underscore');
var Backbone = require('Backbone');

module.exports = Backbone.Model.extend({
    defaults: {
        name: null,
        id: null,
        user: null,
        useCustomOsmBounds: null,
        osmWest: null,
        osmSouth: null,
        osmEast: null,
        osmNorth: null
    },
    urlRoot: 'api/feedcollections/'
});
