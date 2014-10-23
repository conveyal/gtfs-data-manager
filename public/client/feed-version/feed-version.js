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
    urlRoot: 'api/feedversions/',

    change: function () {
      this.attributes.validationResult.errorCount = 0;
      try {
        this.attributes.validationResult.errorCount +=
        this.attributes.validationResult.routes.invalidValues.length;
      } catch (e) {}
      try {
        this.attributes.validationResult.errorCount +=
        this.attributes.validationResult.stops.invalidValues.length;
      } catch (e) {}
      try {
        this.attributes.validationResult.errorCount +=
        this.attributes.validationResult.shapes.invalidValues.length;
      } catch (e) {}
      try {
        this.attributes.validationResult.errorCount +=
        this.attributes.validationResult.trips.invalidValues.length;
      } catch (e) {}
    },

    initialize: function () {
      _.bindAll(this, 'change');
      this.on('add', this.change);
    }
});
