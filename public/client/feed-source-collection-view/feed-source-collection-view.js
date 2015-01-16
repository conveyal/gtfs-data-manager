var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var FeedSource = require('feed-source');
var Handlebars = require('handlebars');
var app = require('application');

var FeedSourceItemView = require('feed-source-item-view');

/**
 * An editable table of FeedSources
 */
module.exports = Backbone.Marionette.CompositeView.extend({
  childView: FeedSourceItemView,
  childViewContainer: 'tbody',
  template: Handlebars.compile(require('./feed-source-collection-view.html')),

  events: {
    'click .newfeedsource': 'add'
  },
  initialize: function(attr) {
    this.feedCollectionId = attr.feedCollectionId;
    _.bindAll(this, 'add');

    // default is to show new feed button
    var showNewFeedButton = _.isUndefined(attr.showNewFeedButton) ? true : attr.showNewFeedButton;

    if (app.user && !app.user.admin) {
      showNewFeedButton = false;
    }

    // use a bare model to pass random bits to the template
    this.model = new Backbone.Model({showNewFeedButton: showNewFeedButton});
  },

  add: function() {
    this.collection.add(
      new FeedSource({
        name: Messages('app.new_feed_source_name'),
        isPublic: true,
        retrievalMethod: 'MANUALLY_UPLOADED',
        url: null,
        feedCollection: {
          id: this.feedCollectionId
        },
        lastUpdated: 0
      })
    );
  }
});
