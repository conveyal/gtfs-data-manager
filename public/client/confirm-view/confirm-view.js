var BB = require('bb');
var _ = require('underscore');
var Handlebars = require('handlebars');

/**
 * usage: new ConfirmView({title: text, body: text, [onProceed: function,] [onCancel: function,]})
 */
module.exports = BB.Marionette.ItemView.extend({
  template: Handlebars.compile(require('./confirm-view.html')),

  events: {
    'click .cancel-action': 'onCancel',
    'click .proceed-action': 'onProceed'
  },

  initialize: function(attr) {
    this.model = new BB.Model({
      title: attr.title,
      body: attr.body
    });

    this.onCancel = !_.isUndefined(attr.onCancel) ? attr.onCancel : function() {};
    this.onProceed = !_.isUndefined(attr.onProceed) ? attr.onProceed : function() {};
  },

  onShow: function() {
    this.$el.find('.modal').modal();
  }
});
