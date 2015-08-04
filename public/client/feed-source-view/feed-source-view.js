/**
 * Show some information about a feed source
 */

var _ = require('underscore');
var app = require('application');
var FeedVersion = require('feed-version');
var FeedVersionView = require('feed-version-view');
var FeedBrandingView = require('feed-branding-view');
var NoteCollectionView = require('note-collection-view');
var FeedVersionNavigationView = require('feed-version-navigation-view');
var LayoutView = require('layout-view');

module.exports = LayoutView.extend({
  template: require("./feed-source-view.html"),
  regions: {
    validationRegion: '#validation',
    brandingRegion: '#branding',
    notesRegion: '.source-notes',
    versionNavigationRegion: '#version-navigation'
  },

  events: {
    'click #share-url': 'doNothing',
    'click .deploy': 'deploy',
    'change #snapshot-version': 'changeSnapshot'
  },

  initialize: function(attr) {
    this.feedVersionId = attr.feedVersionId;
  },

  onShow: function() {
    var instance, version;

    if (this.feedVersionId)
      version = new FeedVersion({
        id: this.feedVersionId
      });

    else
      version = new FeedVersion({
        id: this.model.get('latestVersionId')
      });

    var navBase = [{
      name: this.model.get('feedCollection').name,
      href: '#overview/' + this.model.get('feedCollection').id
    }, {
      name: this.model.get('name'),
      href: '#feed/' + this.model.id
    }];

    if (version.get('id') !== null) {

      instance = this;
      version.fetch().done(function() {
        instance.validationRegion.show(new FeedVersionView({
          model: version
        }));
        instance.versionNavigationRegion.show(new FeedVersionNavigationView({
          model: version
        }));

        // set up nav
        navBase.push({
          name: window.Messages('app.feed_version.version_number', version.get('version')),
          href: '#feed/' + instance.model.id + '/' + version.id
        });

        app.nav.setLocation(navBase);
      });
    } else {
      this.versionNavigationRegion.show(new FeedVersionNavigationView({
        feedSource: this.model
      }));

      // no version to speak of
      app.nav.setLocation(navBase);
    }

    // expose the copypastable URL to allow users to view/edit
    if (app.user.admin) {
      instance = this;
      $.ajax({
        url: 'api/feedsources/' + this.model.get('id') + '/getKey',
        success: function(data) {
          instance.$('#share-url').val(window.location.origin + window.location.pathname + window.location.hash +
            '?userId=' + encodeURIComponent(data.userId) +
            '&key=' + encodeURIComponent(data.key));
        }
      });
    }

    // set up comments
    this.notesRegion.show(new NoteCollectionView({
      objectId: this.model.get('id'),
      type: 'FEED_SOURCE'
    }));

    // set up branding
    this.brandingRegion.show(new FeedBrandingView({
      feedSource: this.model
    }));

    // set up changeable snapshot IDs
    if (this.model.get('retrievalMethod') == 'PRODUCED_IN_HOUSE' && this.model.get('editorId')) {
      $.ajax({
        url: 'api/feedcollections/geteditorsnapshots/' + this.model.get('editorId'),
        success: function (snapshots) {
          snapshots = _.filter(snapshots, function (s) {
            return s.validFrom && s.validTo;
          });

          _.each(snapshots, function (s) {
            $('<option>')
              .attr('value', s.id)
              .prop('selected', s.id == instance.model.get('snapshotVersion'))
              .text(s.name)
              .appendTo(instance.$('#snapshot-version'));
          });
        }
      })
    }
  },

  /** change snapshot version */
  changeSnapshot: function () {
    this.model.set('snapshotVersion', this.$('#snapshot-version').val());
    this.model.save();
  },

  // don't bubble clicks in the input field (e.g. to copy)
  doNothing: function(e) {
    e.preventDefault();
    e.stopPropagation();
  },

  deploy: function(e) {
    $.ajax({
      url: 'api/deployments/fromfeedsource/' + this.model.id,
      method: 'post',
      success: function(data) {
        window.location.hash = '#deployment/' + data.id;
      }
    });
  }
});

/** cache snapshots */
module.exports.snapshots = null;
