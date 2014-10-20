/**
 * Join several strings, with the separator being the first argument
 */

var Handlebars = require('handlebars');

Handlebars.registerHelper(
    'join',
    function () {
        return arguments.slice(1).join(arguments[0]);
    }
);
