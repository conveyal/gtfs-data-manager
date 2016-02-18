var BB = require('bb');
var ItemView = require('item-view');
var _ = require('underscore');

module.exports = ItemView.extend({
  template: require('./update-logo.html'),

  events: {
    'submit form': 'onSubmit'
  },

  initialize: function(attr) {
    this.model = new BB.Model(attr);
  },

  onShow: function() {
    this.$el.find('.modal').modal();
    if(this.options.onShow) this.options.onShow.call(this);
  },

  onSubmit: function(evt) {
    var filename = evt.target[0].value;
    if(!filename || filename.toLowerCase().indexOf('.png', filename.length - 4) === -1) {
      alert('Must be a PNG file');
      evt.preventDefault();
    }
  }
});
