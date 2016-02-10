var CompositeView = require('composite-view');
var _ = require('underscore');
var FeedCollection = require('feed-collection');
var EditableTextWidget = require('editable-text-widget');

var Handlebars = require('handlebars');
var app = require('application');

/**
 * An item view of a single FeedCollection
 */
var FeedCollectionItemView = EditableTextWidget.extend({
  tagName: 'li',
  attribute: 'name',
  className: 'list-group-item',
  href: function() {
    return '#overview/' + this.model.get('id');
  },

  onShow: function() {
    if (typeof(EditableTextWidget.prototype.onShow) == 'function')
      EditableTextWidget.prototype.onShow.call(this);

    if (!this.model.get('id'))
    // new feed
      this.edit();
  }
});

/**
 * An editable view of a FeedCollectionCollection
 */
module.exports = CompositeView.extend({
  template: require('./feed-collection-collection-view.html'),
  childView: FeedCollectionItemView,
  childViewContainer: 'ul',

  // set up event handlers
  events: {
    'click .newfeedcoll': 'add'
  },
  initialize: function() {
    _.bindAll(this, 'add');

    if(this.collection.length === 1) {
      window.location = '/#overview/' + this.collection.models[0].get('id');
    }

    Handlebars.registerHelper(
      'canAdministerApp',
      function(name, options) {
        var canAdministerApp = app.auth0User.canAdministerApp();
        if(canAdministerApp) return options.fn(this);
        return options.inverse(this);
      }
    );
  },

  /** Add an item to the collection */
  add: function() {
    // note that this is not persisted to the server here; it won't be, until they change the name
    // this is by design
    this.collection.add(new FeedCollection({
      name: window.Messages('app.new_feed_collection_name')
    }));
  },

  /*serializeData: function() {
    // include the current user so that the view can figure out what is fair game for editing
    var ret = {
      canAdministerApp: true
    };
    return ret;
    //return Object.assign(ret, this.model.toJSON());
  },*/


});
