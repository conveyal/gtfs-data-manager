/**
 * Show information about a version of a feed
 * This primarily consists of validation results
 */

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var m = require('models');
var Handlebars = require('handlebars');

var InvalidValuesList = Backbone.Marionette.ItemView.extend({
    // rather than having a ton of levels of nested views, since we're not editing, most of the
    // recursion occurs in the template
    template: Handlebars.compile(require('./invalid-values-list.html')),

    initialize: function (attr) {
        // group them by error type
        var invalidValues = attr.invalidValues;
        var errors = {};
        var invalidValuesLen = invalidValues.length;
        for (var i = 0; i < invalidValuesLen; i++) {
            var iv = invalidValues[i];
            
            if (typeof(errors[iv.problemType]) == 'undefined') {
                errors[iv.problemType] = [];
                errors[iv.problemType].name = iv.problemType;
            }
            
            errors[iv.problemType].push(iv);
        }

        // we use a bare Backbone.Model to pass the tree to the template
        this.model = new Backbone.Model({errors: errors});
    }   
});

module.exports = Backbone.Marionette.LayoutView.extend({
    template: Handlebars.compile(require('./feed-version-view.html')),

    regions: {
        routesRegion: '#routes',
        tripsRegion: '#trips',
        stopsRegion: '#stops',
        shapesRegion: '#shapes'
    },

    onShow: function () {
        this.routesRegion.show(new InvalidValuesList({invalidValues: this.model.get('validationResult').routes.invalidValues}));
        this.stopsRegion.show(new InvalidValuesList({invalidValues: this.model.get('validationResult').stops.invalidValues}))
        this.tripsRegion.show(new InvalidValuesList({invalidValues: this.model.get('validationResult').trips.invalidValues}));
        this.shapesRegion.show(new InvalidValuesList({invalidValues: this.model.get('validationResult').shapes.invalidValues}));
    }
});
        
