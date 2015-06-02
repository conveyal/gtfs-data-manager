var app = require('application');
var BB = require('bb');
var CompositeView = require('composite-view');
var FeedSource = require('feed-source');
var FeedSourceItemView = require('feed-source-item-view');
var OkDialogView = require('ok-dialog-view');
var ConfirmView = require('confirm-view');
var Handlebars = require('handlebars');
var _ = require('underscore');

/**
 * An editable table of FeedSources
 */
module.exports = CompositeView.extend({
  childView: FeedSourceItemView,
  childViewContainer: 'tbody',
  template: require('./feed-source-collection-view.html'),

  events: {
    'click .newfeedsource': 'add',
    'click .deploy-public': 'deployPublic',
    'click .fetch-all-feeds': 'fetchAllFeeds',
    'click .sort-by': 'sortBy'
  },

  onRender: function() {
    // set up sort indicators
    this.$('.sort-marker').remove();

    this.$('a[data-attr="' + this.collection.sortAttribute + '"]').parent();

    var span = $('<span>')
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
    var showDeploymentButton = !(app.user && !app.user.admin);

    if (app.user && !app.user.admin || !app.user) {
      showNewFeedButton = false;
      showDeployPublicButton = false;
    }

    // use a bare model to pass random bits to the template
    this.model = new BB.Model({
      feedCollectionId: this.feedCollectionId,
      showNewFeedButton: showNewFeedButton,
      showDeploymentButton: showDeploymentButton,
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
  deployPublic: function() {
    var instance = this;

    app.modalRegion.show(new ConfirmView({
      title: window.Messages('app.deploy-public'),
      body: window.Messages('app.deploy-public.message'),
      onProceed: function () {
        // user feedback
        instance.$('.deploy-public span.glyphicon')
          // set up spinner
          .removeClass('glyphicon-upload').addClass('glyphicon-refresh').addClass('spinner')
          .parent()
          .prop('disabled', true);

        instance.$('.deploy-public span.button-label')
          .text(window.Messages('app.deploy-public.updating'));

        $.post('deployPublic', {
            feedCollectionId: instance.feedCollectionId
          })
          .done(function() {
            instance.$('.deploy-public span.glyphicon')
              // set up spinner
              .addClass('glyphicon-upload').removeClass('glyphicon-refresh').removeClass('spinner')
              .parent()
              .prop('disabled', false);

            instance.$('.deploy-public span.button-label')
              .text(window.Messages('app.deploy-public'));
          });
        }
      }));
  },

  /** sort by a particular column. new feeds are still at the top */
  sortBy: function(e) {
    e.preventDefault();
    var attr = $(e.target).attr('data-attr');

    // sort backwards on second click
    this.collection.sortBackwards = this.collection.sortAttribute == attr && !this.collection.sortBackwards;
    this.collection.sortAttribute = attr;

    this.collection.sort();
  },

  fetchAllFeeds: function() {
    var self = this;
    var dialogTemplate = Handlebars.compile(require('./fetch-all-feeds-results.html'));

    $.ajax({
      url: 'api/feedcollections/' + this.feedCollectionId + '/fetchAllFeeds',
      method: 'POST',
      success: function(data) {
        var results = {};
        var updatedFeeds = false;
        _.each(self.collection.models, function(feedSource) {
          var name = feedSource.get('name');
          if(feedSource.get('id') in data) {
            if(data[feedSource.get('id')] !== null) {
              results[name] = window.Messages('app.fetch_all_feeds.updated');
              updatedFeeds = true;
            }
            else {
              results[name] = window.Messages('app.fetch_all_feeds.no_change');
            }
          }
          else {
            results[name] = window.Messages('app.fetch_all_feeds.not_fetched');
          }
        });
        app.modalRegion.show(new OkDialogView({
          title: window.Messages('app.fetch_all_feeds.results'),
          body: dialogTemplate(results),
          onOk : function() {
            if(updatedFeeds) location.reload();
          }
        }));
      }
    });
  }
});
