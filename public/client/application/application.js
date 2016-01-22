var BB = require('bb');
var Bootstrap = require('bootstrap');
var Breadcrumb = require('breadcrumb-nav');
var es5 = require('es5-shim');
var es6 = require('es6-shim');
var Handlebars = require('Handlebars');

// register Handlebars helpers and partials
require('date-render-helper');
require('class-helper');
require('logic-helper');
require('translate-helper');
require('text-helper');
require('admin-helper');
require('validation-partial');

var app = new BB.Marionette.Application();

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
      console.log('logging out')
      localStorage.removeItem('userToken')
      window.location.hash = '#login';

      /*$.ajax({
        url: 'logout',
      }).done(function(data) {
        if (data.status == 'logged_out') {
          $('#logout').addClass("hidden");
          $('#logged-in-user').text('');
          $('#manageUsers').addClass('hidden');
          $('#myAccount').addClass('hidden');
          window.location.hash = '#login';
        }
      });*/
    });

  $('#manageUsers').text(window.Messages("app.user.manage-users"));
  $('#myAccount').text(window.Messages("app.user.my-account"));


});

module.exports = app;
