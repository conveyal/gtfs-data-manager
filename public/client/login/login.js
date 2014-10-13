var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');

module.exports = function () {
    var Login = Backbone.Marionette.LayoutView.extend({
        template: Handlebars.compile(require('./login.html')),
        events: {'click .login': 'doLogin'},

        initialize: function () {
	    // bind it so context is layout not a DOM object
	    _.bindAll(this, 'doLogin');
        },

        doLogin: function () {
	    $.post('/authenticate',
	           {username: this.$('input[name="username"]').val(),
		    password: this.$('input[name="password"]').val()})
	        .then(function (data) {
		    $('#logged-in-user').text(window.Messages('app.account.logged_in_as', data['username']));
	        })
	        .fail(function () {
		    alert('Log in failed');
	        });

	    return false;
        }
    });

    // show your work
    app.appRegion.show(new Login());
}
