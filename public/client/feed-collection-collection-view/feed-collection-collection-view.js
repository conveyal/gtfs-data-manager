var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var FeedCollection = require('feed-collection');

var Handlebars = require('handlebars');

var EditableTextWidget = require('editable-text-widget');

/**
 * An item view of a single FeedCollection
 */
var FeedCollectionItemView = EditableTextWidget.extend({
    tagName: 'li',
    attribute: 'name',
    href: function () { 
        return '#overview/' + this.model.get('id');
    },

    onShow: function () {
        if (typeof(EditableTextWidget.prototype.onShow) == 'function')
            EditableTextWidget.prototype.onShow.call(this);

        if (this.model.get('id') == null)
            // new feed
            this.edit();
    }
});

/**
 * An editable view of a FeedCollectionCollection
 */
module.exports = Backbone.Marionette.CollectionView.extend({
    template: Handlebars.compile(require('./feed-collection-collection-view.html')),
    childView: FeedCollectionItemView,
    childViewContainer: 'ul',

    // set up event handlers
    events: {
        'click .newfeedcoll': 'add'
    },
    initialize: function () {
        _.bindAll(this, 'add');
    },
    
    /** Add an item to the collection */
    add: function () {
        // note that this is not persisted to the server here; it won't be, until they change the name
        // this is by design
        this.collection.add(new FeedCollection({name: window.Messages('app.new_feed_collection_name')}));
    }
});
