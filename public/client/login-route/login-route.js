var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');

var Login = Backbone.Marionette.LayoutView.extend({
    template: Handlebars.compile(require('./login-route.html')),
    events: {'click .login': 'doLogin'},

    initialize: function (attr) {
        this.returnTo = attr.returnTo
	// bind it so context is layout not a DOM object
	_.bindAll(this, 'doLogin');
    },

    doLogin: function () {
        var instance = this;
	$.post('/authenticate',
	       {username: this.$('input[name="username"]').val(),
		password: this.$('input[name="password"]').val()})
	    .then(function (data) {
		$('#logged-in-user').text(window.Messages('app.account.logged_in_as', data['username']));
                $('#logout').removeClass('hidden');

                // note: log out is handled in application.js

                window.location.hash = instance.returnTo != undefined ? instance.returnTo : '#admin';
	    })
	    .fail(function () {
		alert('Log in failed');
	    });

	return false;
    },

    onShow: function () {
        // init nav
        app.nav.setLocation([
            {name: Messages('app.location.login'), href: '#login'}
        ]);
    }
});


module.exports = function (returnTo) {
    // show your work
    app.appRegion.show(new Login({returnTo: returnTo}));
}
