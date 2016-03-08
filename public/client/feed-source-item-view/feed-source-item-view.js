var _ = require('underscore');
var app = require('application');
var ConfirmView = require('confirm-view');
var EditorAgencyView = require('editor-agency-view');
var EditableTextWidget = require('editable-text-widget');
var LayoutView = require('layout-view');

/**
 * An item view of a single FeedSource
 */
module.exports = LayoutView.extend({
  regions: {
    nameRegion: '.name',
    urlRegion: '.url'
  },
  template: require('./feed-source-item-view.html'),
  tagName: 'tr',

  events: {
    'change .edit-bool': 'editBool',
    'change .feed-source': 'editSource',
    'click .remove-feed': 'removeSource'
  },

  initialize: function() {
    _.bindAll(this, 'editBool', 'editSource', 'removeSource', 'handleUrlRegion');

    var canAdminsterProject = app.auth0User.canAdminsterProject(this.model.get('feedCollection').id);
    this.model.set('canAdminsterProject', canAdminsterProject);

    var canManageFeed = app.auth0User.canManageFeed(this.model.get('feedCollection').id, this.model.get('id'));
    this.model.set('canManageFeed', canManageFeed);

    console.log(this.model)
  },

  // edit a boolean value
  editBool: function(e) {
    var $t = $(e.target);

    var attr = {};
    attr[$t.attr('name')] = $t.is(':checked');

    this.model.set(attr);
    this.model.save();

    // no need to re-render because the checkbox has already been rendered by the browser
  },

  // edit the retrieval method
  editSource: function(e) {
    this.model.set('retrievalMethod', $(e.target).val());
    this.model.save();
  },

  // delete a feed source
  removeSource: function(e) {
    var instance = this;
    app.modalRegion.show(new ConfirmView({
      title: window.Messages('app.confirm'),
      body: window.Messages('app.confirm_delete', this.model.get('name')),
      onProceed: function() {
        instance.model.destroy();
      }
    }));
  },

  onShow: function() {
    var canManageFeed = app.auth0User.canManageFeed(this.model.get('feedCollection').id, this.model.get('id'));

    var nameField = new EditableTextWidget({
      model: this.model,
      attribute: 'name',
      disabled: !canManageFeed,
      href: function() {
        if (this.model.id === null) {
          // make it a no-op until saved
          return '#overview/' + this.model.get('feedCollection').id;
        } else {
          return '#feed/' + this.model.get('id');
        }
      }
    });
    this.nameRegion.show(nameField);

    // start out editing name if it's new; this way we ensure it is saved before
    if (_.isUndefined(this.model.id) || _.isNull(this.model.id))
      nameField.edit();

    this.handleUrlRegion();
    this.model.on('change:retrievalMethod', this.handleUrlRegion);
  },

  // figure out what belongs in the URL region: a URL editor, a GTFS Editor selector, or nothing
  handleUrlRegion: function() {
    var canManageFeed = app.auth0User.canManageFeed(this.model.get('feedCollection').id, this.model.get('id'));

    var retrievalMethod = this.model.get('retrievalMethod');
    if (retrievalMethod == 'FETCHED_AUTOMATICALLY') {
      this.urlRegion.show(new EditableTextWidget({
        model: this.model,
        maxWidth: 35,
        attribute: 'url',
        disabled: !canManageFeed,
        href: function() {
          return this.model.get('url');
        }
      }));
    /*} else if (retrievalMethod == 'PRODUCED_IN_HOUSE') {
      this.urlRegion.show(new EditorAgencyView({
        model: this.model
      }));*/
    } else {
      this.urlRegion.empty();
    }
  }
});
