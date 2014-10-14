var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');

var app = new Backbone.Marionette.Application();

app.addRegions({
    appRegion: '#content'
});

module.exports = app;
