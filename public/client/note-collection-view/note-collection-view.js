/**
 * Show a bunch of notes, comment-style
 */

var BB = require('bb');
var _ = require('underscore');
var Handlebars = require('handlebars');
var NoteCollection = require('note-collection');
var Note = require('note');

var NoteItemView = BB.Marionette.ItemView.extend({
  template: Handlebars.compile(require('./note-item-view.html'))
});

/**
 * Instantiate a new NoteCollectionView like so:
 * new NoteCollectionView({objectId: 'u-u-i-d', type: 'FEED_SOURCE'})
 */
module.exports = BB.Marionette.CompositeView.extend({
  template: Handlebars.compile(require('./note-collection-view.html')),
  childView: NoteItemView,
  className: 'row',
  events: { 'submit form': 'newComment' },

  initialize: function (attr) {
    this.objectId = attr.objectId;
    this.type = attr.type;

    this.collection = new NoteCollection();
    this.collection.fetch({data: {objectId: this.objectId, type: this.type}});

    _.bindAll(this, 'newComment');
  },

  /**
   * Grab the contents of the text field and create a new comment
   */
  newComment: function (e) {
    // don't submit the form
    e.preventDefault();

    var n = new Note({
      objectId: this.objectId,
      type: this.type,
      note: this.$('textarea').val()
    });

    this.$('form').addClass('disabled');

    var instance = this;
    n.save().done(function () {
      instance.collection.add(n);
      instance.$('form').removeClass('disabled');
      instance.$('textarea').val('');
    })
    .fail(function () {
      instance.$('form').removeClass('disabled');
      window.alert('Commenting failed');
    });
  },

  attachHtml: function (collectionView, childView, index) {
    // order not important; new things always go at the bottom
    collectionView.$('.new-comment').before(childView.$el);
  }
});
