// highlight start dates in the future in red
var Handlebars = require('handlebars');

Handlebars.registerHelper(
  'getClassForStartDate',
  function(date) {
    if (new Date().getTime() > date >= 0)
      return '';
    else return 'bg-danger';
  }
);
