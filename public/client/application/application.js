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
    navRegion: '#nav'
});


// initialize breadcrumb navigation
app.nav = new v.BreadCrumb();

app.addInitializer(function () {
    app.navRegion.show(app.nav);
});

module.exports = app;
