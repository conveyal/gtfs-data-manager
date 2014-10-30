var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var FeedCollection = require('feed-collection');
var FeedSourceCollection = require('feed-source-collection');
var FeedSourceCollectionView = require('feed-source-collection-view');
var FeedVersionCollection = require('feed-version-collection');
var FeedVersion = require('feed-version');
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

  usePreviousVersion: function (e) {
    e.preventDefault();
    if (this.model.get('previousVersionId') !== null) {
      this.switchVersion(this.model.get('previousVersionId'));
    }
  },

  useNextVersion: function (e) {
    e.preventDefault();
    if (this.model.get('nextVersionId') !== null) {
      this.switchVersion(this.model.get('nextVersionId'));
    }
  },

  /** Utility function to replace this feed version with a different one */
  switchVersion: function (version) {
    var newVersion = new FeedVersion({id: version});
    var instance = this;
    newVersion.fetch({data: {summarized: 'true'}}).done(function () {
      instance.collection.remove(instance.model, {silent: true});
      instance.collection.add(newVersion);
    });
  }
});

module.exports = Backbone.Marionette.CompositeView.extend({
  template: Handlebars.compile(require('./deployment-view.html')),
  childView: FeedVersionDeploymentView,
  childViewContainer: 'tbody',

  initialize: function () {
    this.collection = new FeedVersionCollection(this.model.get('feedVersions'));
    _.bindAll(this, 'collectionChange');
  },

  collectionChange: function () {
    this.model.set('feedVersions', this.collection.toJSON());
    this.model.save();
  },

  buildChildView: function (child, ChildViewClass, childViewOptions) {
    var opts = _.extend({model: child, collection: this.collection}, childViewOptions);
    return new ChildViewClass(opts);
  },

  onShow: function () {
    // show the invalid feed sources (i.e. sources with no current loadable version)
    this.invalidFeedSourceRegion = new Backbone.Marionette.Region({
      el: '.invalid-feed-sources'
    });

    this.collection.on('remove', this.collectionChange);
    this.collection.on('add', this.collectionChange);

    var invalid = new FeedSourceCollection(this.model.get('invalidFeedSources'));
    this.invalidFeedSourceRegion.show(new FeedSourceCollectionView({collection: invalid, showNewFeedButton: false}));
  }
});
