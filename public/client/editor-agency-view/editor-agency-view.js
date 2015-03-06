var BB = require('bb');
var Handlebars = require('handlebars');
var _ = require('underscore');

module.exports = BB.Marionette.ItemView.extend({
  template: Handlebars.compile(require('./editor-agency-view.html')),

  events: {
    'change select': 'changeAgency'
  },

  initialize: function() {
    this.agencies = [];
    var instance = this;

    if (module.exports.agencies === null) {
      module.exports.agencies = $.ajax({
        url: 'api/feedcollections/geteditoragencies'
      });
    }

    // even if it is already done, this will still get called
    module.exports.agencies.done(function(data) {
      instance.agencies = data;
      // this may trigger rendering more than once; oh well.
      instance.render();
    });

    _.bindAll(this, 'changeAgency');
  },

  onRender: function() {
    // select the appropriate agency
    this.$('option[value="' + this.model.get('editorId') + '"]').prop('selected', true);
  },

  changeAgency: function() {
    var newAgency = this.$('select').val();
    this.model.set('editorId', newAgency);
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
