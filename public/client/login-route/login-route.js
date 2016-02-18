var _ = require('underscore');
var app = require('application');
var LayoutView = require('layout-view');

var Login = LayoutView.extend({
  template: require('./login-route.html'),
  events: {
    'click .login': 'doLogin'
  },

  initialize: function(attr) {
    this.returnTo = attr.returnTo
      // bind it so context is layout not a DOM object
    //_.bindAll(this, 'doLogin');
  },

  onShow: function() {
    // init nav
    app.nav.setLocation([{
      name: Messages('app.location.login'),
      href: '#login'
    }]);

    var lock = new Auth0Lock(app.config.auth0ClientId, app.config.auth0Domain);
    var lockOptions = {
      connections: ['Username-Password-Authentication'],
      closable: false
    };
    if (app.config.logo) lockOptions.icon = app.config.logo;

    lock.show(lockOptions, function (err, profile, token) {
      if(err) {
        console.log(err)
      } else {
        // save profile and token to localStorage
        localStorage.setItem('userToken', token);

        app.userLoggedIn(token, profile, lock);
        document.location.hash = ''
      }
    }, {
      container: 'auth0login'
    });
  }
});

module.exports = function(returnTo) {
  // show your work
  app.appRegion.show(new Login({
    returnTo: returnTo
  }));
}
