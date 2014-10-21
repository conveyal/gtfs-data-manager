var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var Handlebars = require('Handlebars');
var Breadcrumb = require('breadcrumb-nav');

// register Handlebars helpers and partials
require('date-render-helper');
require('class-helper');
require('logic-helper');
require('translate-helper');
require('text-helper');
require('admin-helper');
require('validation-partial');

var app = new Backbone.Marionette.Application();

app.user = null;

app.addRegions({
    appRegion: '#content',
    navRegion: '#nav',
    modalRegion: '#modal'
});

// initialize breadcrumb navigation
app.nav = new Breadcrumb();

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
