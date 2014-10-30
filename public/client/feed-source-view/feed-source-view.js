/**
 * Show some information about a feed source
 */

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');
var FeedVersion = require('feed-version');
var FeedVersionView = require('feed-version-view');
var NoteCollectionView = require('note-collection-view');
var FeedVersionNavigationView = require('feed-version-navigation-view');

module.exports = Backbone.Marionette.LayoutView.extend({
  template: Handlebars.compile(require("./feed-source-view.html")),
  regions: {
    validationRegion: '#validation',
    notesRegion: '.source-notes',
    versionNavigationRegion: '#version-navigation'
  },

  events: {
    'click #share-url': 'doNothing'
  },
  initialize: function(attr) {
    this.feedVersionId = attr.feedVersionId;
  },

  onShow: function() {
    var version;

    if (this.feedVersionId != undefined && this.feedVersionId != null)
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

    var instance = this;
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
  }

  else {
    this.versionNavigationRegion.show(new FeedVersionNavigationView({
      feedSource: this.model
    }));

    // no version to speak of
    app.nav.setLocation(navBase);
  }

  // expose the copypastable URL to allow users to view/edit
  if (app.user.admin) {
    var instance = this;
    $.ajax({
      url: 'api/feedsources/' + this.model.get('id') + '/getKey',
      success: function(data) {
        instance.$('#share-url').val(window.location.origin + window.location.pathname + window.location.hash +
          '?userId=' + encodeURIComponent(data['userId']) +
          '&key=' + encodeURIComponent(data['key']));
      }
    });
  }

  // set up comments
  this.notesRegion.show(new NoteCollectionView({
    objectId: this.model.get('id'),
    type: 'FEED_SOURCE'
  }));
},

// don't bubble clicks in the input field (e.g. to copy)
doNothing: function(e) {
  e.preventDefault();
  e.stopPropagation();
}

})
