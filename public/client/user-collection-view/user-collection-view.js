/**
 * A view of a number of users.
 *
 * It's safe to assume the user is an admin as this isn't used in any other contexts.
 * Security is implemented on the server anyhow.
 */

 var BB = require('bb');
 var Handlebars = require('handlebars');
 var UserCollection = require('user-collection');
 var User = require('user');

 var UserItemView = BB.Marionette.ItemView.extend({
   template: Handlebars.compile(require('./user-item-view.html')),
   tagName: 'tr'
 });

 module.exports = BB.Marionette.CompositeView.extend({
   template: Handlebars.compile(require('./user-collection-view.html')),
   childView: UserItemView,
   childViewContainer: 'tbody'
 });
