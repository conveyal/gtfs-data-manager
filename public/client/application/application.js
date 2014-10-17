var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var Handlebars = require('Handlebars');
var v = require('views');

// register Handlebars helpers
var helpers = require('helpers');

for (var i = 0; i < helpers.length; i++) {
    Handlebars.registerHelper(helpers[i][0], helpers[i][1]);
}

var app = new Backbone.Marionette.Application();

app.addRegions({
    appRegion: '#content',
    navRegion: '#nav',
    modalRegion: '#modal'
});

// initialize breadcrumb navigation
app.nav = new v.BreadCrumb();

app.addInitializer(function () {
    app.navRegion.show(app.nav);

    // set up the name
    $('#appName').text(Messages('app.name'));

    $('#logout').text(Messages('app.logout'))
        .click(function (e) {
            e.preventDefault();
            $.ajax({
                url: 'logout',
            }).done(function (data) {
                if (data.status == 'logged_out') {
                    $('#logout').addClass("hidden");
                    $('#logged-in-user').text('');
                    window.location.hash = '#login';
                }
            });
        });
});

module.exports = app;
