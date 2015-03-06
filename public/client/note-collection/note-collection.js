/**
 * A collection of notes
 */

var BB = require('bb');
var Note = require('note');

module.exports = BB.Collection.extend({
  model: Note,
  url: 'api/notes'
});
