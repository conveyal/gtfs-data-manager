var app = require('application');
var Deployment = require('deployment');
var DeploymentView = require('deployment-view');
var Handlebars = require('handlebars.js');

module.exports = function(deploymentId) {
  var d = new Deployment({
    id: deploymentId
  });

  var targets;

  var tDf = $.ajax({
    url: 'api/deployments/targets',
    success: function(data) {
      targets = data;
    }
  });

  var depDf = d.fetch();

  $.when(tDf, depDf).then(function() {
    app.appRegion.show(new DeploymentView({
      model: d,
      targets: targets
    }));

    // nav
    app.nav.setLocation([{
      name: d.get('feedCollection').name,
      href: '#overview/' + d.get('feedCollection').id
    }, {
      name: window.Messages('app.deployment.deployments'),
      href: '#deployments/' + d.get('feedCollection').id
    }, {
      name: d.get('name'),
      href: '#deployment/' + deploymentId
    }]);
  });
};
