var app = require('application');
var BB = require('bb');
var Auth0User = require('auth0-user');

var Router = BB.Router.extend({
  routes: {
    "": "admin",
    "login": "login",
    "login/*returnTo": "login",
    "admin": "admin",
    "overview/:feedCollectionId": "overview",
    "osmconfig/:feedCollectionId": "osmconfig",
    "otpconfig/:id": "otpConfig",
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
  otpConfig: require('otp-config-route'),
  users: require('user-collection-route'),
  user: require('user-route'),
  newUser: require('new-user-route')
});

// start up the app
$(document).ready(function() {
  var router;

  var userToken = localStorage.getItem('userToken');
  //console.log('found userToken', userToken)

  if(userToken && userToken !== "null") {
    console.log('found token');
    $.ajax({
      url: "https://conveyal.eu.auth0.com/tokeninfo",
      data: {
        id_token: userToken
      },
      contentType: "application/json",
      success: function(data) {
        app.userLoggedIn(userToken, data);

        startApp();
      },
      error: function() {
        console.log('tokeninfo problem, logging out');
        app.logout();
      }
    });

  }
  else {
    // check for SSO login
    $.ajax({
      url: 'auth0Config',
      success: function(data) {
        var lock = new Auth0Lock(data.client_id, data.domain);

        // check if this is an SSO callback
        var hash = lock.parseHash(window.location.hash);
        if (hash && hash.id_token) {
          // the user came back from the login (either SSO or regular login),
          // save the token
          localStorage.setItem('userToken', hash.id_token);

          // redirect to "targetUrl" if any
          window.location.href = hash.state || '';
          return;
        }

        // check if logged in elsewhere via SSO
        lock.$auth0.getSSOData(function(err, data) {
          if (!err && data.sso) {
            // there is! redirect to Auth0 for SSO
            lock.$auth0.signin({
              callbackOnLocationHash: true
            });
          } else { // assume that we are not logged in
            // don't let us go to #login/login/login/login/login/overview
            if (window.location.hash.indexOf('login') != 1
              && !/\#feed\/[0-9a-z\-]+\?userId=.+&key=.+/.exec(window.location.hash)) {
              document.location.hash = '#login/' + window.location.hash.slice(1);
            }
            startApp();
          }
        });
      }
    });

  }

});

function startApp() {
  app.start();
  router = new Router();
  BB.history.start();
}
