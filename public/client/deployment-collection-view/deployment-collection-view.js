var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars');

var DeploymentItemView = Backbone.Marionette.ItemView.extend({
  tagName: 'tr',
  template: Handlebars.compile(require('./deployment-item-view.html'))
});

module.exports = Backbone.Marionette.CompositeView.extend({
  template: Handlebars.compile(require('./deployment-collection-view.html')),
  childView: DeploymentItemView,
  childViewContainer: 'tbody'
});
