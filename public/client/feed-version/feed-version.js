/*
 * A model representing a feed version
 */

var BB = require('bb');
var _ = require('underscore');

module.exports = BB.Model.extend({
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

  change: function() {
    var vr = this.get('validationResult');

    if (!_.isUndefined(vr) && _.isUndefined(vr.errorCount)) {
      vr.errorCount = 0;
      try {
        vr.errorCount += vr.routes.invalidValues.length;
      } catch (e) {}
      try {
        vr.errorCount += vr.stops.invalidValues.length;
      } catch (e) {}
      try {
        vr.errorCount += vr.shapes.invalidValues.length;
      } catch (e) {}
      try {
        vr.errorCount += vr.trips.invalidValues.length;
      } catch (e) {}

      this.set('validationResult', vr);
    }
  },

  initialize: function() {
    _.bindAll(this, 'change');
    this.on('add', this.change);
    this.change();
  }
});
