var _ = require('underscore');
var Backbone = require('backbone');
var $ = Backbone.$ = require('jquery');
var app = require('application');

var Router = Backbone.Router.extend({
    routes: {
	"login": "login"
    },

    login: require('login')
});

// start up the app
$(document).ready(function () {
    app.start();
    var r = new Router();
    Backbone.history.start();
});
