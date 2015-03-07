var app = require('application');
var FeedCollection = require('feed-collection');
var FeedSourceCollection = require('feed-source-collection');
var View = require('otp-config-view');

module.exports = function(id) {
  var fc = new FeedCollection({
    id: id
  });
  var fcDf = fc.fetch();

  var fsc = new FeedSourceCollection();
  var fscDf = fsc.fetch({
    data: {
      feedcollection: id
    }
  });

  $.when(fcDf, fscDf).done(function() {
    app.appRegion.show(new View({
      model: fc,
      feedSources: fsc
    }));

    // nav
    app.nav.setLocation([{
      name: fc.get('name'),
      href: '#overview/' + fc.get('id')
    }, {
      name: window.Messages('app.otp_config.title'),
      href: '#otpconfig'
    }]);
  });
};
