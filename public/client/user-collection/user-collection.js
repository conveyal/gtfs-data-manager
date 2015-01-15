var _ = require('underscore');
var Backbone = require('Backbone');
var User = require('user');

module.exports = Backbone.Collection.extend({
  model: User,
  url: 'api/users'
});
