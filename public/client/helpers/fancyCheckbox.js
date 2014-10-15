// TODO: make fancy editable view for this

var Handlebars = require('handlebars');

module.exports = [
    'fancyCheckbox',
    function (val) {
        if (val) {
            return new Handlebars.SafeString('<span class="glyphicon glyphicon-ok" title="' + 
                                         Handlebars.escapeExpression(Messages('app.yes'))
                                             + '"></span>');
        }
        else {
            return new Handlebars.SafeString('<span class="glyphicon glyphicon-remove" title="' + 
                                         Handlebars.escapeExpression(Messages('app.no'))
                                             + '"></span>');
        }
    }
];
