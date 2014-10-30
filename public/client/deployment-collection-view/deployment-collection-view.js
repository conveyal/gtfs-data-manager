var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars');
var Deployment = require('deployment');

var DeploymentItemView = Backbone.Marionette.ItemView.extend({
  tagName: 'tr',
  template: Handlebars.compile(require('./deployment-item-view.html'))
});

module.exports = Backbone.Marionette.CompositeView.extend({
  template: Handlebars.compile(require('./deployment-collection-view.html')),
  childView: DeploymentItemView,
  childViewContainer: 'tbody',

  events: { 'click .new-deployment': 'deploy'},

  initialize: function (attr) {
    this.feedCollectionId = attr.feedCollectionId;

    _.bindAll(this, 'deploy');
  },

  /**
   * Create a new deployment of this feedcollection
   */
  deploy: function() {
    var d = new Deployment({
      feedCollection: {id: this.feedCollectionId},
      name: window.Messages('app.deployment.default_name')
    });
    d.save().done(function() {
      window.location.hash = '#deployment/' + d.id;
    });
  }
});
