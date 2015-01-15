var app = require('application');
var User = require('user');
var UserView = require('user-view');

module.exports = function () {
  // create a blank user, and allow the user to edit it
  // but only if the user is an admin
  if (!app.user.admin) {
    window.location.hash = '#';
    return;
  }

  var u = new User();
  var uv = new UserView({model: u});
  app.appRegion.show(uv);

  app.nav.setLocation([
    {name: window.Messages('app.users'), href: '#users'},
    {name: window.Messages('app.user.new'), href: '#users/new'}
  ]);
};
