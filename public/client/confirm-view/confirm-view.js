var BB = require('bb');
var ItemView = require('item-view');
var _ = require('underscore');

/**
 * usage: new ConfirmView({title: text, body: text, [onProceed: function,] [onCancel: function,]})
 */
module.exports = ItemView.extend({
  template: require('./confirm-view.html'),

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
    if(this.options.onShow) this.options.onShow.call(this);
  }
});
