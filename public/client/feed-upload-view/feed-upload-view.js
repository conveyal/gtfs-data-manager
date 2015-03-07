/*
 * Upload a feed to the manager manually.
 */

var ItemView = require('item-view');

module.exports = ItemView.extend({
  template: require('./feed-upload-view.html'),
  onShow: function() {
    this.$('.modal').modal();
  }
});
