var app = module.exports = new Backbone.Marionette.Application();
var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');

app.addRegions({
    appRegion: '#content'
});

app.addInitializer(function () {
    var Router = require('routes');
    new Router();
});

app.start();
