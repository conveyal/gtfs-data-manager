/**
 * Show some information about a feed source
 */

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');
var m = require('models');
var v = require('views');

var FeedSourceLayout = Backbone.Marionette.LayoutView.extend({
    template: Handlebars.compile(require("./FeedSource.html")),
    regions: {latestValidationRegion: '#latest-validation'},
    
    onShow: function () {
        var latest = new m.FeedVersion({id: this.model.get('latestVersionId')});
        var instance = this;
        latest.fetch().done(function () {
            instance.latestValidationRegion.show(new v.FeedVersion({model: latest}));
        });

        // set up nav
        app.nav.setLocation([
            {name: this.model.get('feedCollection').name, href: '#overview/' + this.model.get('feedCollection').id},
            {name: this.model.get('name'), href: '#feed/' + this.model.get('id')},
        ]);
    }
})

module.exports = function (feedSourceId) {
    var model = new m.FeedSource({id: feedSourceId});
    model.fetch().done(function () {
        app.appRegion.show(new FeedSourceLayout({model: model}));
    });
};
