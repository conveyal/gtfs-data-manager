/*
 * Upload a feed to the manager manually.
 */

var BB = require('bb');
var Handlebars = require('handlebars');

module.exports = BB.Marionette.ItemView.extend({
  template: Handlebars.compile(require('./feed-upload-view.html')),
  onShow: function() {
    this.$('.modal').modal();
  }
});
