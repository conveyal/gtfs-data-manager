// equality, for use with handlebars if statements

var Handlebars = require('handlebars');

Handlebars.registerHelper(
  'eq',
  function(v1, v2) {
    return v1 == v2;
  }
);
