var _ = require('underscore');
var Backbone = require('backbone');
var $ = Backbone.$ = require('jquery');
var app = require('application');

var Router = Backbone.Router.extend({
    routes: {
        "": "admin",
	"login": "login",
        "admin": "admin",
        "overview/:feedCollectionId": "overview",
        "feed/:feedSourceId": "feedsource"
    },

    login: require('login'),
    admin: require('admin'),
    feedsource: require('feedsource'),
    overview: require('overview'),
});

// start up the app
$(document).ready(function () {
    var router;

    // determine whether the user is logged in or not
    $.ajax({
        url: '/loggedInUser'
    })
        .always(function () {
            app.start();
            router = new Router();
            Backbone.history.start();
        })
        .done(function (data) {
            $('#logged-in-user').text(window.Messages('app.account.logged_in_as', data['username']));
            $('#logout').removeClass('hidden');
        })
        .fail(function () {
            // assume that we are not logged in
            document.location.hash = '#login';
        });
});
