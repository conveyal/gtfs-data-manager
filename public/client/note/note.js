var BB = require('bb');

module.exports = BB.Model.extend({
  defaults: {
    user: null,
    note: null
  },

  urlRoot: 'api/notes'
});
