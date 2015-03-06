var app = require('application');
var DeploymentCollection = require('deployment-collection');
var DeploymentCollectionView = require('deployment-collection-view');
var FeedCollection = require('feed-collection');
var _ = require('underscore');

module.exports = function(feedCollectionId) {
  var d = new DeploymentCollection();
  var dDf = d.fetch({
    data: {
      feedCollection: feedCollectionId
    }
  });

  var fc = new FeedCollection({
    id: feedCollectionId
  });
  var fcDf = fc.fetch();

  $.when(dDf, fcDf).done(function() {
    d = d.filter(function(deployment) {
      var fsId = deployment.get('feedSourceId');
      return fsId === null || _.isUndefined(fsId);
    });

    d = new DeploymentCollection(d);

    app.appRegion.show(new DeploymentCollectionView({
      collection: d,
      model: fc
    }));

    // nav
    app.nav.setLocation([{
      name: fc.get('name'),
      href: '#overview/' + fc.get('id')
    }, {
      name: window.Messages('app.deployment.deployments'),
      href: '#deployments'
    }]);
  });
};
