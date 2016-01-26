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

  onShow: function () {
    this.sizeTable();
    $(window).on('resize.feed-source-collection-view', this.sizeTable);
    this.collection.on('add', this.sizeTable);
  },

  onBeforeDestroy: function () {
    $(window).off('resize.feed-source-collection-view');
  },

  /** Size the columns and height */
  sizeTable: function () {

    var table = this.$('#feedsources');

    // figure the height
    var height = $(window).height() - table.find('tbody').offset().top - 50;

    table.find('tbody').css('height', height + 'px');

    // and the widths
    // (relative to the size of a checkbox)
    var widths = [6, 1.3, 1.8, 6, 8, 3, 1.5, 1.5, 1.5, 1.5, 1.5, 3, 3, 1.2];
    var scale = (table.width() - 100) / _.reduce(widths, function (a, b) { return a + b });

    for (var i = 0; i < widths.length; i++) {
      table.find('thead th:nth-child(' + (i + 1) + '), tr td:nth-child(' + (i + 1) + ')')
        .css('width', (widths[i] * scale) + 'px');
    }

    // width of the "feed did not load successfully" message
    // + 20 handles the gutters that are left out within this cell
    var msgWidth = _.reduce(widths.slice(7, 13), function (a, b) { return a + b }) * scale + 20;
    table.find('.loadFailureReason')
      .css('width', msgWidth + 'px')
      .next()
      .css('width', (widths[widths.length - 1] * scale) + 'px');

    // make each td as tall as its tr so that the backgrounds extend to the edges
    table.find('td').each(function () {
      $(this).css('height', $(this).parent().height() + 'px');
    });
  },

  initialize: function(attr) {
    this.feedCollectionId = attr.feedCollectionId;
    _.bindAll(this, 'add', 'deployPublic', 'sortBy', 'sizeTable');

    // default is to show new feed button
    var showNewFeedButton = _.isUndefined(attr.showNewFeedButton) ? true : attr.showNewFeedButton;
    var showDeployPublicButton = showNewFeedButton;
    var showDownloadButton = true;
    var showDeploymentButton = !(app.user && !app.user.admin);

    if (app.auth0User && !app.auth0User.canAdminsterProject(this.feedCollectionId) || !app.auth0User) {
      showNewFeedButton = false;
      showDeployPublicButton = false;
      showDownloadButton = false;
    }

    // use a bare model to pass random bits to the template
    this.model = new BB.Model({
      feedCollectionId: this.feedCollectionId,
      showNewFeedButton: showNewFeedButton,
      showDeploymentButton: showDeploymentButton,
      showDeployPublicButton: showDeployPublicButton,
      showDownloadButton: showDownloadButton
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

    this.sizeTable();
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
