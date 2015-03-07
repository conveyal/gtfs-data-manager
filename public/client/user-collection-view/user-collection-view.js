/**
 * A view of a number of users.
 *
 * It's safe to assume the user is an admin as this isn't used in any other contexts.
 * Security is implemented on the server anyhow.
 */

var CompositeView = require('composite-view');
var ItemView = require('item-view');
var UserCollection = require('user-collection');
var User = require('user');

var UserItemView = ItemView.extend({
  template: require('./user-item-view.html'),
  tagName: 'tr'
});

module.exports = CompositeView.extend({
  template: require('./user-collection-view.html'),
  childView: UserItemView,
  childViewContainer: 'tbody'
});
