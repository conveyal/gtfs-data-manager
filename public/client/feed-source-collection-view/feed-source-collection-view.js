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
    'click .newfeedsource': 'add',
    'click .deploy-public': 'deployPublic'
  },
  initialize: function(attr) {
    this.feedCollectionId = attr.feedCollectionId;
    _.bindAll(this, 'add', 'deployPublic');

    // default is to show new feed button
    var showNewFeedButton = _.isUndefined(attr.showNewFeedButton) ? true : attr.showNewFeedButton;

    var showDeployPublicButton;

    if (app.user && app.user.admin) {
      showDeployPublicButton = true;
    } else {
      showNewFeedButton = false;
      showDeployPublicButton = false;
    }

    // use a bare model to pass random bits to the template
    this.model = new Backbone.Model({
      showNewFeedButton: showNewFeedButton,
      showDeployPublicButton: showDeployPublicButton
    });
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
  },

  /**
   * deploy the public feeds to a directory, where they are presumably consumed
   * by a front-end web server.
   */
   deployPublic: function () {
     var instance = this;

     // user feedback
     this.$('.deploy-public span.glyphicon')
      // set up spinner
      .removeClass('glyphicon-upload').addClass('glyphicon-refresh').addClass('spinner')
      .parent()
      .prop('disabled', true);

    this.$('.deploy-public span.button-label')
      .text(window.Messages('app.deploy-public.updating'));

    $.post('deployPublic', {feedCollectionId: this.feedCollectionId})
      .done(function () {
        instance.$('.deploy-public span.glyphicon')
        // set up spinner
          .addClass('glyphicon-upload').removeClass('glyphicon-refresh').removeClass('spinner')
          .parent()
          .prop('disabled', false);

        instance.$('.deploy-public span.button-label')
        .text(window.Messages('app.deploy-public'));
      });
   }

});
