var ItemView = require('item-view');
var _ = require('underscore');

module.exports = ItemView.extend({
  template: require('./editor-agency-view.html'),

  events: {
    'change .agency': 'changeAgency',
    'change .snapshot': 'changeSnapshot'
  },

  initialize: function() {
    this.agencies = [];
    this.snapshots = [];
    var instance = this;

    if (module.exports.agencies === null) {
      module.exports.agencies = $.ajax({
        url: 'api/feedcollections/geteditoragencies'
      });

      module.exports.snapshots = $.ajax({
        url: 'api/feedcollections/geteditorsnapshots'
      });
    }

    // even if it is already done, this will still get called
    $.when(module.exports.agencies, module.exports.snapshots).then(function(agencies, snapshots) {
      instance.agencies = agencies[0];
      // don't include snapshots that don't have dates
      instance.snapshots = _.filter(snapshots[0], function (snap) {
        return snap.validFrom && snap.validTo;
      });
      // this may trigger rendering more than once; oh well.
      instance.render();
    });

    _.bindAll(this, 'changeAgency');
  },

  onRender: function() {
    // select the appropriate agency
    this.$('.agency option[value="' + this.model.get('editorId') + '"]').prop('selected', true);
    this.updateSnapshots();
  },

  changeAgency: function() {
    var newAgency = this.$('.agency').val();
    this.model.set('editorId', newAgency);
    this.updateSnapshots();
    // don't save incompatible agencies and snapshots
    // changeSnapshot will perform the save
    this.changeSnapshot();
  },

  changeSnapshot: function () {
    var newSnap = this.$('.snapshot').val();
    this.model.set('snapshotVersion', newSnap);
    this.model.save();
  },

  /** update the snapshot select list */
  updateSnapshots: function () {
    var agencyId = this.$('.agency').val();
    var agencySnapshots = _.where(this.snapshots, {agencyId: agencyId});

    var sel = this.$('.snapshot').empty();

    var instance = this;

    // make a blank option so that we don't automatically select a snapshot
    $('<option></option>').appendTo(sel);

    _.each(agencySnapshots, function(snap) {
      $('<option></option>')
        .attr('value', snap.id)
        .text(snap.name + " (" + snap.version + ")")
        .prop('selected', snap.id === instance.model.get('snapshotVersion'))
        .appendTo(sel);
    });
  },

  serializeData: function() {
    return _.extend({
      agencies: this.agencies
    }, this.model.toJSON());
  }
});

/** Cache the agencies */
module.exports.agencies = null;
module.exports.snapshots = null;
