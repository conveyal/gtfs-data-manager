var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');
var Deployment = require('deployment');
var DeploymentView = require('deployment-view');

module.exports = function(deploymentId) {
  var d = new Deployment({
    id: deploymentId
  });

  d.fetch().done(function() {
    app.appRegion.show(new DeploymentView({
      model: d
    }));

    // nav
    app.nav.setLocation([{
      name: d.get('feedCollection').name,
      href: '#overview/' + d.get('feedCollection').id
    }, {
      name: window.Messages('app.deployment.edit-deployment'),
      href: '#deployment/' + deploymentId
    }]);
  });
};
