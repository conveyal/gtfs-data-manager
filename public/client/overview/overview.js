var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');
var m = require('models');
var v = require('views');

var Overview = Backbone.Marionette.LayoutView.extend({
    regions: {feedSourceRegion: '#feed-sources'},
    template: Handlebars.compile(require('./overview.html')),

    initialize: function (attr) {
        this.feedCollectionId = attr.feedCollectionId;
    },

    onShow: function () {
        var feedSources = new m.FeedSourceCollection();
        var instance = this;
        feedSources.fetch({data: {feedcollection: this.feedCollectionId}}).done(function () {
            instance.feedSourceRegion.show(new v.FeedSourceEditableView({
                collection: feedSources,
                feedCollectionId: instance.feedCollectionId
            }));
        });

        var feedCollection = new m.FeedCollection({id: this.feedCollectionId});
        feedCollection.fetch().done(function () {
            app.nav.setLocation([
                {name: feedCollection.get('name'), href: '#overview/' + feedCollection.get('id')}
            ]);
        });
    }
});

module.exports = function (feedCollectionId) {
    app.appRegion.show(new Overview({feedCollectionId: feedCollectionId}));
}
