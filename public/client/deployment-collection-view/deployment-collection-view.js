var BB = require('bb');
var Deployment = require('deployment');
var Handlebars = require('handlebars');
var _ = require('underscore');

var DeploymentItemView = BB.Marionette.ItemView.extend({
  tagName: 'tr',
  template: Handlebars.compile(require('./deployment-item-view.html'))
});

module.exports = BB.Marionette.CompositeView.extend({
  template: Handlebars.compile(require('./deployment-collection-view.html')),
  childView: DeploymentItemView,
  childViewContainer: 'tbody',

  events: {
    'click .new-deployment': 'deploy'
  },

  initialize: function() {
    _.bindAll(this, 'deploy');
  },

  /**
   * Create a new deployment of this feedcollection
   */
  deploy: function() {
    var now = new Date();

    var date = String(now.getFullYear());
    date += String(now.getMonth() + 1 < 10 ? '0' + (now.getMonth() + 1) : now.getMonth() + 1);
    date += String(now.getDate() < 10 ? '0' + now.getDate() : now.getDate());

    var d = new Deployment({
      feedCollection: {
        id: this.model.id
      },
      name: this.model.get('name').toLowerCase().replace(/ /g, '-').replace(/[^a-z0-9-]/g, '') + '-' + date
    });
    d.save().done(function() {
      window.location.hash = '#deployment/' + d.id;
    });
  }
});
