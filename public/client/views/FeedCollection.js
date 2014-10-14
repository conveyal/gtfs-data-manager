var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars');

module.exports.FeedCollectionView = Backbone.Marionette.ItemView.extend({
    template: Handlebars.compile(require('./FeedCollection.html')),
    tagName: 'li'
});

module.exports.FeedCollectionCollectionView = Backbone.Marionette.CollectionView.extend({
    childView: module.exports.FeedCollectionView,
    tagName: 'ul'
});
