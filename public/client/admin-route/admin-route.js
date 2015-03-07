var app = require('application');
var FeedCollectionCollectionView = require('feed-collection-collection-view');
var FeedCollectionCollection = require('feed-collection-collection');
var LayoutView = require('layout-view');

module.exports = function() {
  var Admin = LayoutView.extend({
    regions: {
      collectionRegion: '#collection'
    },
    template: require('./admin-route.html'),
    onShow: function() {
      var agencies = new FeedCollectionCollection();
      var instance = this;
      agencies.fetch().done(function() {
        instance.collectionRegion.show(new FeedCollectionCollectionView({
          collection: agencies
        }));
      })

      app.nav.setLocation([]);
    }
  });

  // show your work
  app.appRegion.show(new Admin());
}
