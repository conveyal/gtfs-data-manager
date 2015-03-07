var BB = require('bb');
var CompositeView = require('composite-view');
var _ = require('underscore');
var FeedCollection = require('feed-collection');
var FeedSourceCollection = require('feed-source-collection');
var FeedSourceCollectionView = require('feed-source-collection-view');
var FeedVersionCollection = require('feed-version-collection');
var FeedVersion = require('feed-version');
var ItemView = require('item-view');
var EditableTextWidget = require('editable-text-widget');
var app = require('application');
var ConfirmView = require('confirm-view');
var DeploymentProgressView = require('deployment-progress-view');

// FeedVersionItemView is already used on the versions page, so let's keep class names unique
var FeedVersionDeploymentView = ItemView.extend({
  template: require('./feed-version-deployment-view.html'),
  tagName: 'tr',
  events: {
    'click .remove-version': 'removeVersion',
    'click .use-previous-version': 'usePreviousVersion',
    'click .use-next-version': 'useNextVersion'
  },

  initialize: function() {
    _.bindAll(this, 'removeVersion', 'usePreviousVersion', 'useNextVersion');
  },

  removeVersion: function(e) {
    e.preventDefault();

    this.collection.remove(this.model);
  },

  usePreviousVersion: function(e) {
    e.preventDefault();
    if (this.model.get('previousVersionId') !== null) {
      this.switchVersion(this.model.get('previousVersionId'));
    }
  },

  useNextVersion: function(e) {
    e.preventDefault();
    if (this.model.get('nextVersionId') !== null) {
      this.switchVersion(this.model.get('nextVersionId'));
    }
  },

  /** Utility function to replace this feed version with a different one */
  switchVersion: function(version) {
    var newVersion = new FeedVersion({
      id: version
    });
    var instance = this;
    newVersion.fetch({
      data: {
        summarized: 'true'
      }
    }).done(function() {
      // TODO: this generates two round trips to the server, which is not necessary
      // and is slow, but safe
      instance.collection.add(newVersion);
      instance.collection.remove(instance.model);
    });
  }
});

module.exports = CompositeView.extend({
  template: require('./deployment-view.html'),
  childView: FeedVersionDeploymentView,
  childViewContainer: 'tbody',

  events: {
    'click .deploy': 'deploy'
  },

  initialize: function(attr) {
    this.collection = new FeedVersionCollection(this.model.get('feedVersions'));

    // possible deployment targets
    this.targets = attr.targets;

    _.bindAll(this, 'collectionChange', 'deploy');
  },

  collectionChange: function() {
    this.model.set('feedVersions', this.collection.toJSON());
    this.model.save();
  },

  /**
   * Tell the server to push a deployment to OTP.
   */
  deploy: function(e) {
    e.preventDefault();

    var instance = this;

    var $t = $(e.target);

    // make sure they mean it
    app.modalRegion.show(new ConfirmView({
      title: window.Messages('app.confirm'),
      // todo: multiple servers
      body: window.Messages('app.deployment.confirm', instance.model.get('name'), $t.attr('name')),
      onProceed: function() {
        $.ajax({
          url: 'api/deployments/' + instance.model.id + '/deploy/' + $t.attr('name'),
          method: 'POST',
          success: function() {
            // refetch the deployment, to show where it is deployed to
            instance.model.fetch().done(function() {
              instance.render();
              instance.onShow();
            });

            // show the status of the deployment
            // TODO: don't hardcode target
            app.modalRegion.show(new DeploymentProgressView({
              deployment: instance.model,
              target: $t.attr('name')
            }));
          },
          statusCode: {
            503: function() {
              window.alert(window.Messages('app.deployment.already_deploying'))
            }
          }
        });
      }
    }))
  },

  buildChildView: function(child, ChildViewClass, childViewOptions) {
    var opts = _.extend({
      model: child,
      collection: this.collection
    }, childViewOptions);
    return new ChildViewClass(opts);
  },

  onShow: function() {
    // show the invalid feed sources (i.e. sources with no current loadable version)
    this.invalidFeedSourceRegion = new BB.Marionette.Region({
      el: '.invalid-feed-sources'
    });

    var invalid = new FeedSourceCollection(this.model.get('invalidFeedSources'));
    this.invalidFeedSourceRegion.show(new FeedSourceCollectionView({
      collection: invalid,
      showNewFeedButton: false
    }));

    // show the name, in an editable fashion
    this.nameRegion = new BB.Marionette.Region({
      el: '#deployment-name'
    });

    this.nameRegion.show(new EditableTextWidget({
      model: this.model,
      attribute: 'name',
      href: window.location.hash
    }));

    this.collection.on('remove', this.collectionChange);
    // We don't save on add, because, for now, adds only happen before removes, which do trigger saves
    // TODO fix
    //this.collection.on('add', this.collectionChange);
  },

  // we need to pass deployment targets to the view as well
  serializeData: function() {
    return _.extend({
      targets: this.targets
    }, this.model.toJSON());
  }
});
