// A list of FeedVersions for a particular FeedSource

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars');
var app = require('application');

var FeedSource = require('feed-source');

var FeedVersionItemView = Backbone.Marionette.ItemView.extend({
    template: Handlebars.compile(require('./feed-version-item-view.html')),
    tagName: 'tr'                                 
});

module.exports = Backbone.Marionette.CompositeView.extend({
    childView: FeedVersionItemView,
    childViewContainer: 'tbody',
    template: Handlebars.compile(require('./feed-version-collection-view.html')),

    initialize: function () {
        // extract the feed source
        this.model = new FeedSource(this.collection.at(0).get('feedSource'));
    },

    onShow: function () {
        app.nav.setLocation([
            {name: this.collection.at(0).get('feedSource').feedCollection.name, href: '#overview/' + this.collection.at(0).get('feedSource').feedCollection.id},
            {name: this.collection.at(0).get('feedSource').name, href: '#feed/' + this.collection.at(0).get('feedSource').id},
            {name: window.Messages('app.feed_version.versions'), href: '#versions/' + this.collection.at(0).get('feedSource').id}
        ]);
    }
 });
