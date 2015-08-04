var ItemView = require('item-view');
var _ = require('underscore');

module.exports = ItemView.extend({
  template: require('./editor-agency-view.html'),

  events: {
    'change .agency': 'changeAgency'
  },

  initialize: function() {
    this.agencies = [];
    this.snapshots = [];
    var instance = this;

    if (module.exports.agencies === null) {
      module.exports.agencies = $.ajax({
        url: 'api/feedcollections/geteditoragencies'
      });
    }

    // even if it is already done, this will still get called
    module.exports.agencies.then(function(agencies) {
      instance.agencies = agencies;
      // this may trigger rendering more than once; oh well.
      instance.render();
    });

    _.bindAll(this, 'changeAgency');
  },

  onRender: function() {
    // select the appropriate agency
    this.$('.agency option[value="' + this.model.get('editorId') + '"]').prop('selected', true);
  },

  changeAgency: function() {
    var newAgency = this.$('.agency').val();
    this.model.set('editorId', newAgency);
    this.model.set('snapshotVersion', null);
    this.model.save();
  },

  serializeData: function() {
    return _.extend({
      agencies: this.agencies
    }, this.model.toJSON());
  }
});

/** Cache the agencies */
module.exports.agencies = null;
