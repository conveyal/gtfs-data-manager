var _ = require('underscore');
var Backbone = require('Backbone');

module.exports = Backbone.Model.extend({
  defaults: {
    user: null,
    note: null
  },

  urlRoot: 'api/notes'
});
