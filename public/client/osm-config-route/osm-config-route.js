var FeedCollection = require('feed-collection');
var FeedSourceCollection = require('feed-source-collection');
var OsmConfigView = require('osm-config-view');
var app = require('application');

module.exports = function(feedCollectionId) {

  var fc = new FeedCollection({
    id: feedCollectionId
  });
  var fcDf = fc.fetch();

  var fsc = new FeedSourceCollection();
  var fscDf = fsc.fetch({
    data: {
      feedcollection: feedCollectionId
    }
  });

  $.when(fcDf, fscDf).done(function() {

    app.appRegion.show(new OsmConfigView({
      model: fc,
      feedSources: fsc
    }));

    // nav
    app.nav.setLocation([{
      name: fc.get('name'),
      href: '#overview/' + fc.get('id')
    }, {
      name: window.Messages('app.osm_config.configure'),
      href: '#osmconfig'
    }]);
  });
};
