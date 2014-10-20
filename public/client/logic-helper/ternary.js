// a ternary operator
// use like ? condition ifTrue ifFalse

var Handlebars = require('handlebars');

Handlebars.registerHelper(
    '?',
    function (cond, tru, fals) {
        return cond ? tru : fals;
    }
);
