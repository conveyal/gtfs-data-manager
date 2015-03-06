// Is the current user an admin

var Handlebars = require('handlebars');

Handlebars.registerHelper(
  'admin',
  function() {
    // we have to have the require here, because application depends on this file
    return require('application').user.admin;
  }
);
