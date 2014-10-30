var DeploymentCollection = require('deployment-collection');
var DeploymentCollectionView = require('deployment-collection-view');
var FeedCollection = require('feed-collection');
var app = require('application');

module.exports = function (feedCollectionId) {
  var d = new DeploymentCollection();
  d.fetch({data: {feedCollection: feedCollectionId}}).done(function () {
    app.appRegion.show(new DeploymentCollectionView({collection: d, feedCollectionId: feedCollectionId}));
  });

  var fc = new FeedCollection({id: feedCollectionId});
  fc.fetch().done(function () {
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
