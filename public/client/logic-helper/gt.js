// greater than

var Handlebars = require('handlebars');

Handlebars.registerHelper(
    'gt',
    function (v1, v2) {
        return v1 > v2;
    }
);
