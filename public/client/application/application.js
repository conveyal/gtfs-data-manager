var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var Handlebars = require('Handlebars');

// register Handlebars helpers
var helpers = require('helpers');

for (var i = 0; i < helpers.length; i++) {
    Handlebars.registerHelper(helpers[i][0], helpers[i][1]);
}

var app = new Backbone.Marionette.Application();

app.addRegions({
    appRegion: '#content'
});

module.exports = app;
