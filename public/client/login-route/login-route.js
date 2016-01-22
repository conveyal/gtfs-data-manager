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

  /*doLogin: function() {
    var instance = this;
    $.post('/authenticate', {
        username: this.$('input[name="username"]').val(),
        password: this.$('input[name="password"]').val()
      })
      .then(function(data) {
        $('#logged-in-user').text(window.Messages('app.account.logged_in_as', data.username));
        $('#logout').removeClass('hidden');
        $('#myAccount').removeClass('hidden').attr('href', '#user/' + data.id);

        if (data.admin)
          $('#manageUsers').removeClass('hidden');

        // note: log out is handled in application.js

        app.user = data;

        window.location.hash = instance.returnTo ? instance.returnTo : '#admin';
      })
      .fail(function() {
        window.alert('Log in failed');
      });

    return false;
  },*/

  onShow: function() {
    // init nav
    app.nav.setLocation([{
      name: Messages('app.location.login'),
      href: '#login'
    }]);

    var lock = new Auth0Lock('ztOnBIopl4YwuEYnhEItPuwQA0p4IG1Y', 'conveyal.eu.auth0.com');
    lock.show(function(err, profile, token) {
      // save profile and token to localStorage
      console.log('token', token)
      localStorage.setItem('userToken', token);
      localStorage.setItem('userProfile', JSON.stringify(profile));
      document.location.hash = ''
    }, {
      container: 'auth0login',
      authParams: { scope: 'openid email' }
    });
    /*lock.show({
      container: 'auth0login',
      callbackURL: 'http://localhost:9000/',
      responseType: 'code', authParams: {
        scope: 'openid profile'
      }
    });*/
  }
});

module.exports = function(returnTo) {
  // show your work
  app.appRegion.show(new Login({
    returnTo: returnTo
  }));
}
