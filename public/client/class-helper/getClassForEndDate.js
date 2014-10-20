// highlight in red if past, yellow if within two weeks

var Handlebars = require('handlebars');

Handlebars.registerHelper(
    'getClassForEndDate',
    function (date) {
        var daysToExpiration = (date - new Date().getTime()) / (60 * 60 * 24 * 1000);

        if (daysToExpiration > 14) {
            return '';
        }
        else if (daysToExpiration >= 0) {
            return 'bg-warning';
        }
        else return 'bg-danger';
    }
);
