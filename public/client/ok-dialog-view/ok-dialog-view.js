var BB = require('bb');
var ItemView = require('item-view');
var _ = require('underscore');


/**
 * usage: new ConfirmView({title: text, body: text, [onProceed: function,] [onCancel: function,]})
 */
module.exports = ItemView.extend({
  template: require('./ok-dialog-view.html'),

  events: {
    'click .ok-action': 'onOk'
  },


  initialize: function(attr) {
    this.model = new BB.Model({
      title: attr.title,
      body: attr.body
    });

    this.onOk = !_.isUndefined(attr.onOk) ? attr.onOk : function() {};
  },

  onShow: function() {
    this.$el.find('.modal').modal();
  }
});
