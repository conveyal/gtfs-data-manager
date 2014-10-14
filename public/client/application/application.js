var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var Handlebars = require('Handlebars');

Handlebars.registerHelper('t', window.Messages);

var app = new Backbone.Marionette.Application();

app.addRegions({
    appRegion: '#content'
});

module.exports = app;
