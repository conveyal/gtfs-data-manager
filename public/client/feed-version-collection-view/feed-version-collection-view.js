// A list of FeedVersions for a particular FeedSource

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars');

var FeedVersionItemView = Backbone.Marionette.ItemView.extend({
    template: Handlebars.compile(require('./feed-version-item-view.html')),
    tagName: 'tr'                                 
});

module.exports = Backbone.Marionette.CompositeView.extend({
    childView: FeedVersionItemView,
    childViewContainer: 'tbody',
    template: Handlebars.compile(require('./feed-version-collection-view.html')),
 });
