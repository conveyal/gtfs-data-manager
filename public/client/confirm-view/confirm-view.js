var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var _ = require('underscore');
var Handlebars = require('handlebars');

/**
 * usage: new ConfirmView({title: text, body: text, [onProceed: function,] [onCancel: function,]})
 */
module.exports = Backbone.Marionette.ItemView.extend({
  template: Handlebars.compile(require('./confirm-view.html')),

  events: {
    'click .cancel-action': 'onCancel',
    'click .proceed-action': 'onProceed'
  },

  initialize: function (attr) {
    this.model = new Backbone.Model({
      title: attr.title,
      body: attr.body
    });

    this.onCancel = !_.isUndefined(attr.onCancel) ? attr.onCancel : function () {};
    this.onProceed = !_.isUndefined(attr.onProceed) ? attr.onProceed : function () {};
  },

  onShow: function () {
    this.$('.modal').modal();
  }
});
