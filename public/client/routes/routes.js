var _ = require('underscore');
var Backbone = require('backbone');
var $ = Backbone.$ = require('jquery');
var app = require('application');

var Router = Backbone.Router.extend({
    routes: {
	"login": "login",
        "admin": "admin",
        "overview/:feedCollectionId": "overview"
    },

    login: require('login'),
    admin: require('admin'),
    overview: require('overview')
});

// start up the app
$(document).ready(function () {
    app.start();
    var r = new Router();
    Backbone.history.start();
});
