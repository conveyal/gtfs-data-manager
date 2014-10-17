/*
 * A model representing a feed version
 */

var _ = require('underscore');
var Backbone = require('Backbone');

module.exports = Backbone.Model.extend({
    defaults: {
        id: null,
        feedSource: null,
        user: null,
        notes: null,
        validationResults: null,
        updated: null,
        version: null
    },
    urlRoot: 'api/feedversions/'
});

        
