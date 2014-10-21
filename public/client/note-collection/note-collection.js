/**
 * A collection of notes
 */

var _ = require('underscore');
var Backbone = require('Backbone');
var Note = require('note');

module.exports = Backbone.Collection.extend({
   model: Note,
   url: 'api/notes'
});
