var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');
var FeedSourceCollection = require('feed-source-collection');
var FeedCollection = require('feed-collection');
var FeedSourceCollectionView = require('feed-source-collection-view');
var Deployment = require('deployment');

var Overview = Backbone.Marionette.LayoutView.extend({
    regions: {feedSourceRegion: '#feed-sources'},
    template: Handlebars.compile(require('./feed-collection-route.html')),

    events: { 'click .deploy': 'deploy' },

    initialize: function (attr) {
        this.feedCollectionId = attr.feedCollectionId;

        _.bindAll(this, 'deploy');
    },


  /**
   * Create a new deployment of this feedcollection
   */
   deploy: function () {
     var d = new Deployment({feedCollection: {id: this.feedCollectionId}});
     d.save().done(function () {
       window.location.hash = '#deployment/' + d.id;
     });
   },

    onShow: function () {
        var feedSources = new FeedSourceCollection();
        var instance = this;
        feedSources.fetch({data: {feedcollection: this.feedCollectionId}}).done(function () {
            instance.feedSourceRegion.show(new FeedSourceCollectionView({
                collection: feedSources,
                feedCollectionId: instance.feedCollectionId
            }));
        });

        var feedCollection = new FeedCollection({id: this.feedCollectionId});
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
