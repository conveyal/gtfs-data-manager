var BB = require('bb');
var User = require('user');

module.exports = BB.Collection.extend({
  model: User,
  url: 'api/users'
});
