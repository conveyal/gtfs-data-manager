var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var FeedCollection = require('feed-collection');
var FeedSourceCollection = require('feed-source-collection');
var FeedSourceCollectionView = require('feed-source-collection-view');
var FeedVersionCollection = require('feed-version-collection');
var Handlebars = require('handlebars');

// FeedVersionItemView is already used on the versions page, so let's keep class names unique
var FeedVersionDeploymentView = Backbone.Marionette.ItemView.extend({
  template: Handlebars.compile(require('./feed-version-deployment-view.html')),
  tagName: 'tr',
  events: {
    'click .remove-version': 'removeVersion',
    'click .use-previous-version': 'usePreviousVersion',
    'click .use-next-version': 'useNextVersion'
  },

  initialize: function () {
    _.bindAll(this, 'removeVersion', 'usePreviousVersion', 'useNextVersion');
  },

  removeVersion: function (e) {
    e.preventDefault();

    this.collection.remove(this.model);
  },

  usePreviousVersion: function () {},

  useNextVersion: function () {}
});

module.exports = Backbone.Marionette.CompositeView.extend({
  template: Handlebars.compile(require('./deployment-view.html')),
  childView: FeedVersionDeploymentView,
  childViewContainer: 'tbody',

  initialize: function () {
    this.collection = new FeedVersionCollection(this.model.get('feedVersions'));
  },

  onShow: function () {
    // show the invalid feed sources (i.e. sources with no current loadable version)
    this.invalidFeedSourceRegion = new Backbone.Marionette.Region({
      el: '.invalid-feed-sources'
    });

    var instance = this;
    this.children.each(function (child) {
      child.collection = instance.collection;
    });

    this.collection.on('remove', function () {
      instance.model.set('feedVersions', instance.collection.toJSON());
      instance.model.save();
    });

    var invalid = new FeedSourceCollection(this.model.get('invalidFeedSources'));
    this.invalidFeedSourceRegion.show(new FeedSourceCollectionView({collection: invalid, showNewFeedButton: false}));
  }
});
