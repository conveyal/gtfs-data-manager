/**
 * Show information about a version of a feed
 * This primarily consists of validation results
 */

var BB = require('bb');
var _ = require('underscore');
var Handlebars = require('handlebars');
var app = require('application');
var NoteCollectionView = require('note-collection-view');
var FeedUploadView = require('feed-upload-view');
var FeedSource = require('feed-source');
var FeedVersion = require('feed-version');

var InvalidValuesList = BB.Marionette.ItemView.extend({
  // rather than having a ton of levels of nested views, since we're not editing, most of the
  // recursion occurs in the template
  template: Handlebars.compile(require('./invalid-values-list.html')),

  initialize: function(attr) {
    // group them by error type

    var showRoute = attr.showRoute || false;

    var invalidValues = attr.invalidValues;
    var errors = {};
    var invalidValuesLen = invalidValues.length;
    for (var i = 0; i < invalidValuesLen; i++) {
      var iv = invalidValues[i];

      if (typeof(errors[iv.problemType]) == 'undefined') {
        errors[iv.problemType] = [];
        errors[iv.problemType].name = iv.problemType;
      }

      if (showRoute) {
        if (iv.route === null || iv.route === undefined)
          iv.route_name = null;

        else if (iv.route.shortName !== null && iv.route.longName !== null)
          iv.route_name = iv.route.shortName + ' ' + iv.route.longName;

        else if (iv.route.shortName !== null)
          iv.route_name = iv.route.shortName;

        else if (iv.route.longName !== null)
          iv.route_name = iv.route.longName;

        else
          iv.route_name = null;
      }

      errors[iv.problemType].push(iv);
    }

    // we use a bare Backbone.Model to pass the tree to the template
    this.model = new BB.Model({
      showRoute: showRoute,
      title: Messages('app.feed_version.' + attr.type + '_warnings'),
      type: attr.type,
      errors: errors
    });
  }
});

module.exports = BB.Marionette.LayoutView.extend({
  template: Handlebars.compile(require('./feed-version-view.html')),

  regions: {
    routesRegion: '#routes',
    tripsRegion: '#trips',
    stopsRegion: '#stops',
    shapesRegion: '#shapes',
    notesRegion: '.version-notes'
  },

  onShow: function() {
    var result = this.model.get('validationResult');
    try {
      var invalidRoutes = result.routes.invalidValues;
      if (invalidRoutes && invalidRoutes.length > 0) {
        this.routesRegion.show(new InvalidValuesList({
          invalidValues: invalidRoutes,
          type: 'route'
        }));
      }
    } catch (e) {}

    try {
      var invalidStops = result.stops.invalidValues;
      if (invalidStops && invalidStops.length > 0) {
        this.stopsRegion.show(new InvalidValuesList({
          invalidValues: invalidStops,
          type: 'stop'
        }));
      }
    } catch (e) {}

    try {
      var invalidTrips = result.trips.invalidValues;
      if (invalidTrips && invalidTrips.length > 0) {
        this.tripsRegion.show(new InvalidValuesList({
          invalidValues: invalidTrips,
          type: 'trip',
          showRoute: true
        }));
      }
    } catch (e) {}

    try {
      var invalidShapes = result.shapes.invalidValues;
      if (invalidShapes && invalidShapes.length > 0) {
        this.shapesRegion.show(new InvalidValuesList({
          invalidValues: invalidShapes,
          type: 'shape'
        }));
      }
    } catch (e) {}

    // set up notes
    this.notesRegion.show(new NoteCollectionView({
      objectId: this.model.get('id'),
      type: 'FEED_VERSION'
    }));
  }
});
