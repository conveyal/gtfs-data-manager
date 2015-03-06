// non-equality for handlebars if statements

var Handlebars = require('handlebars');

Handlebars.registerHelper(
  'neq',
  function(v1, v2) {
    return v1 != v2;
  }
);
