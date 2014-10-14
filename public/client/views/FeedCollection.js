var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var m = require('models');
var Handlebars = require('handlebars');

/**
 * An item view of a single FeedCollection
 */
var FeedCollectionItemView = Backbone.Marionette.ItemView.extend({
    template: Handlebars.compile(require('./FeedCollection.html')),
    tagName: 'li',
    events: { 
        'click .toggle-edit': 'edit'
    },

    initialize: function () {
        _.bindAll(this, 'edit');
        // keep track of whether the field is currently being edited or not
        this.editing = false;
    },

    // toggle editing of a field
    edit: function (e) {
        this.$('.input').toggleClass('hidden');

        this.$('.glyphicon').toggleClass('glyphicon-pencil').toggleClass('glyphicon-ok');

        if (this.editing) {
            // save
            this.model.set({name: this.$('input').val()});
            this.model.save();

            this.$('.label').text(Messages('app.save'));
            this.render();
        }
        else {
            this.$('.label').text(Messages('app.edit'));
        }
        
        this.editing = !this.editing;
    }
});

/**
 * An immutable view of a FeedCollectionCollection
 */
module.exports.FeedCollectionCollectionView = Backbone.Marionette.CollectionView.extend({
    childView: FeedCollectionItemView,
    tagName: 'ul'
});

/**
 * An editable view of a FeedCollectionCollection
 */
module.exports.FeedCollectionEditableView = Backbone.Marionette.LayoutView.extend({
    regions: { collectionRegion: '#feedcollection' },
    template: Handlebars.compile(require('./FeedCollectionEditable.html')),

    // set up event handlers
    events: {
        'click .newfeedcoll': 'add'
    },
    initialize: function () {
        _.bindAll(this, 'add');
    },

    // show the child view
    onShow: function () {
        // pass the collection down
        this.collectionRegion.show(new module.exports.FeedCollectionCollectionView({collection: this.collection}))
    },
    
    /** Add an item to the collection */
    add: function () {
        // note that this is not persisted to the server here; it won't be, until they change the name
        // this is by design
        this.collection.add(new m.FeedCollection({name: window.Messages('app.new_feed_collection_name')}));
    }
});
