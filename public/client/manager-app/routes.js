var _ = require('underscore');
var Backbone = require('backbone');
var $ = Backbone.$ = require('jquery');

var Router = Backbone.Router.extend({
    routes: {
	"login": "login"
    },

    login: require('login')
});

new Router();
