var app = require('application');
var BB = require('bb');

var Router = BB.Router.extend({
  routes: {
    "": "admin",
    "login": "login",
    "login/*returnTo": "login",
    "admin": "admin",
    "overview/:feedCollectionId": "overview",
    "osmconfig/:feedCollectionId": "osmconfig",
    "feed/:feedSourceId/:feedVersionId?userId=:userId&key=:key": "autologin",
    "feed/:feedSourceId/:feedVersionId": "feedsource",
    "feed/:feedSourceId?userId=:userId&key=:key": "autologin",
    "feed/:feedSourceId": "feedsource",
    "versions/:feedSourceId": "versions",
    "deployment/:deploymentId": "deployment",
    "deployments/:feedCollectionId": "deployments",
    "user/:userid": "user",
    "users": "users",
    "users/new": "newUser"
  },

  login: require('login-route'),
  admin: require('admin-route'),
  feedsource: require('feed-source-route'),
  overview: require('feed-collection-route'),
  osmconfig: require('osm-config-route'),
  autologin: require('autologin-route'),
  versions: require('feed-version-collection-route'),
  deployment: require('deployment-route'),
  deployments: require('deployment-collection-route'),
  users: require('user-collection-route'),
  user: require('user-route'),
  newUser: require('new-user-route')
});

// start up the app
$(document).ready(function() {
  var router;

  // determine whether the user is logged in or not
  $.ajax({
      url: '/loggedInUser'
    })
    .done(function(data) {
      $('#logged-in-user').text(window.Messages('app.account.logged_in_as', data.username));
      $('#logout').removeClass('hidden');

      $('#myAccount').removeClass('hidden').attr('href', '#user/' + data.id);

      if (data.admin)
        $('#manageUsers').removeClass('hidden');

      app.user = data;
    })
    .fail(function() {
      // assume that we are not logged in
      // don't let us go to #login/login/login/login/login/overview
      if (window.location.hash.indexOf('login') != 1 && !/\#feed\/[0-9a-z\-]+\?userId=.+&key=.+/.exec(window.location
          .hash))
        document.location.hash = '#login/' + window.location.hash.slice(1);
    })
    .always(function() {
      app.start();
      router = new Router();
      BB.history.start();
    });
});
