// and for handlebars if statements

var Handlebars = require('handlebars');
var _ = require('underscore');

Handlebars.registerHelper(
  'and',
  function() {
    return _.reduce(arguments, function(val, memo) {
      return val && memo;
    });
  }
);
