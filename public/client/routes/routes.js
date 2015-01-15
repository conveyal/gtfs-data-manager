var _ = require('underscore');
var Backbone = require('backbone');
var $ = Backbone.$ = require('jquery');
var app = require('application');

var Router = Backbone.Router.extend({
  routes: {
    "": "admin",
    "login": "login",
    "login/*returnTo": "login",
    "admin": "admin",
    "overview/:feedCollectionId": "overview",
    "feed/:feedSourceId/:feedVersionId?userId=:userId&key=:key": "autologin",
    "feed/:feedSourceId/:feedVersionId": "feedsource",
    "feed/:feedSourceId?userId=:userId&key=:key": "autologin",
    "feed/:feedSourceId": "feedsource",
    "versions/:feedSourceId": "versions",
    "deployment/:deploymentId": "deployment",
    "deployments/:feedCollectionId": "deployments",
    "users": "users"
  },

  login: require('login-route'),
  admin: require('admin-route'),
  feedsource: require('feed-source-route'),
  overview: require('feed-collection-route'),
  autologin: require('autologin-route'),
  versions: require('feed-version-collection-route'),
  deployment: require('deployment-route'),
  deployments: require('deployment-collection-route'),
  users: require('user-collection-route')
});

// start up the app
$(document).ready(function() {
  var router;

  // determine whether the user is logged in or not
  $.ajax({
      url: '/loggedInUser'
    })
    .always(function() {
      app.start();
      router = new Router();
      Backbone.history.start();
    })
    .done(function(data) {
      $('#logged-in-user').text(window.Messages('app.account.logged_in_as', data['username']));
      $('#logout').removeClass('hidden');
      app.user = data;
    })
    .fail(function() {
      // assume that we are not logged in
      // don't let us go to #login/login/login/login/login/overview
      if (window.location.hash.indexOf('login') != 1)
        document.location.hash = '#login/' + window.location.hash.slice(1);
    });
});
