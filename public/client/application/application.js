var BB = require('bb');
var Bootstrap = require('bootstrap');
var Breadcrumb = require('breadcrumb-nav');
var es5 = require('es5-shim');
var es6 = require('es6-shim');
var Handlebars = require('Handlebars');
var Auth0User = require('auth0-user');

// register Handlebars helpers and partials
require('date-render-helper');
require('class-helper');
require('logic-helper');
require('translate-helper');
require('text-helper');
//require('admin-helper');
require('validation-partial');

//var app = new BB.Marionette.Application();

var App = BB.Marionette.Application.extend({

  userLoggedIn: function(token, profile, lock) {

    this.auth0User = new Auth0User(profile);

    $('#logged-in-user').text(window.Messages('app.account.logged_in_as', this.auth0User.getEmail()));
    $('#logout').removeClass('hidden');
    $('#myAccount').removeClass('hidden');

    this.initBB(token);

    // set up single logout
    setInterval(function() {
      // if the token is not in local storage, there is nothing to check (i.e. the user is already logged out)
      if (!localStorage.getItem('userToken')) return;

      lock.$auth0.getSSOData(function(err, data) {
        // if there is still a session, do nothing
        if (err || (data && data.sso)) return;

        // if we get here, it means there is no session on Auth0,
        // then remove the token and redirect to #login
        localStorage.removeItem('userToken');
        window.location.href = '/'
      });
    }, 5000)
  },

  initBB: function(token) {
    BB.$.ajaxSetup({
      beforeSend(jqXHR) {
        jqXHR.setRequestHeader('Authorization', 'Bearer ' + token);
        return true;
      }
    });
  },

  logout: function() {
    localStorage.removeItem('userToken')

    // logout from the data manager server
    $.ajax('logout').done(function(data) {
      // logout from Auth0, redirecting to the manager home page
      var loc = window.location;
      var redirect = loc.protocol + "//" + loc.hostname + (loc.port ? ':' + loc.port: '');
      window.location.replace('https://conveyal.eu.auth0.com/v2/logout?returnTo=' + redirect);
    });
  }
});

var app = new App();

app.user = null;

app.addRegions({
  appRegion: '#content',
  navRegion: '#nav',
  modalRegion: '#modal'
});

// initialize breadcrumb navigation
app.nav = new Breadcrumb();

app.on('before:start', function() {
  app.navRegion.show(app.nav);

  // set up the name
  $('#appName').text(Messages('app.name'));

  $('#logout').text(Messages('app.logout'))
    .click(function(e) {
      e.preventDefault();
      app.logout();
    });

  $('#manageUsers').text(window.Messages("app.user.manage-users"));
  $('#myAccount').text(window.Messages("app.user.my-account"));


});

module.exports = app;
