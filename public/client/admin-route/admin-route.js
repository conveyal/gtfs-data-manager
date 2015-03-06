var app = require('application');
var BB = require('bb');
var FeedCollectionCollectionView = require('feed-collection-collection-view');
var FeedCollectionCollection = require('feed-collection-collection');
var Handlebars = require('handlebars.js');

module.exports = function () {
    var Admin = BB.Marionette.LayoutView.extend({
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
