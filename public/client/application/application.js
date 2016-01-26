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

  userLoggedIn: function(token, profile) {

    this.auth0User = new Auth0User(profile);

    $('#logged-in-user').text(window.Messages('app.account.logged_in_as', this.auth0User.getEmail()));
    $('#logout').removeClass('hidden');
    $('#myAccount').removeClass('hidden');

    this.initBB(token);
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

    $.ajax({
      url: 'logout',
    }).done(function(data) {
      if (data.status == 'logged_out') {
        console.log("logout success");
        $('#logout').addClass("hidden");
        $('#logged-in-user').text('');
        $('#manageUsers').addClass('hidden');
        $('#myAccount').addClass('hidden');
        window.location.hash = '#login';
      }
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
