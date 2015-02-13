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
    'click .deploy-public': 'deployPublic',
    'click .sort-by': 'sortBy'
  },

  onRender: function () {
    // set up sort indicators
    this.$('.sort-marker').remove();

    this.$('a[data-attr="' + this.collection.sortAttribute + '"]').parent();

    var span =   $('<span>')
    .addClass('glyphicon')
    .addClass('sort-marker')
    .addClass(this.collection.sortBackwards ? 'glyphicon-sort-by-alphabet-alt' : 'glyphicon-sort-by-alphabet')
    .appendTo(this.$('a[data-attr="' + this.collection.sortAttribute + '"]').parent());
  },

  initialize: function(attr) {
    this.feedCollectionId = attr.feedCollectionId;
    _.bindAll(this, 'add', 'deployPublic', 'sortBy');

    // default is to show new feed button
    var showNewFeedButton = _.isUndefined(attr.showNewFeedButton) ? true : attr.showNewFeedButton;
    var showDeployPublicButton = showNewFeedButton;

    if (app.user && !app.user.admin || !app.user) {
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
   },

   /** sort by a particular column. new feeds are still at the top */
   sortBy: function (e) {
     e.preventDefault();
     var attr = $(e.target).attr('data-attr');

     // sort backwards on second click
     this.collection.sortBackwards = this.collection.sortAttribute == attr && !this.collection.sortBackwards;
     this.collection.sortAttribute = attr;

     this.collection.sort();
   }
});
