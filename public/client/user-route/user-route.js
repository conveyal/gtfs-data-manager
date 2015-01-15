var User = require('user');
var app = require('application');
var UserView = require('user-view');

module.exports = function (userid) {
  var u = new User({id: userid});
  u.fetch().done(function () {
    app.appRegion.show(new UserView({model: u}));
    app.nav.setLocation([
      {name: window.Messages('app.users'), href: '#users'},
      {name: u.get('username'), href: '#user/' + u.id}
      ]);
  });
};
