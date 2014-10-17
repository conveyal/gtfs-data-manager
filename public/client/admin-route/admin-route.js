var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');
var FeedCollectionCollectionView = require('feed-collection-collection-view');
var FeedCollectionCollection = require('feed-collection-collection');

module.exports = function () {
    var Admin = Backbone.Marionette.LayoutView.extend({
        regions: {collectionRegion: '#collection'},
        template: Handlebars.compile(require('./admin-route.html')),
        onShow: function () {
            var agencies = new FeedCollectionCollection();
            var instance = this;
            agencies.fetch().done(function () {
                instance.collectionRegion.show(new FeedCollectionCollectionView({collection: agencies}));
            })

            app.nav.setLocation([]);
        }
    });
    
    // show your work
    app.appRegion.show(new Admin());
}
