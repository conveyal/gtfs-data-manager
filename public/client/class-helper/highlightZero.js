// return a class to highlight something that equals zero

var Handlebars = require('handlebars');

Handlebars.registerHelper(
  'highlightZero',
  function(value) {
    return value == 0 ? 'bg-danger' : '';
  }
);
