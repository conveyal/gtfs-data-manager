var Handlebars = require('handlebars.js');
var toCapitalCase = require('to-capital-case');

var partials = {
  input: require('./input.html')
};

var helpers = {
  toCapitalCase: toCapitalCase
};

var key;
for (key in partials)
  Handlebars.registerPartial(key, partials[key]);

for (key in helpers)
  Handlebars.registerHelper(key, helpers[key]);

module.exports = Handlebars;
