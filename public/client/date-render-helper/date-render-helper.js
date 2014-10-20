// a helper to render a date in milliseconds since epoch as something a normal
// human can understand

var Handlebars = require('handlebars');

// get the month in the local language
var getMonth = function (month) {
    return [
        Messages('app.date.january'),
        Messages('app.date.february'),
        Messages('app.date.march'),
        Messages('app.date.april'),
        Messages('app.date.may'),
        Messages('app.date.june'),
        Messages('app.date.july'),
        Messages('app.date.august'),
        Messages('app.date.september'),
        Messages('app.date.october'),
        Messages('app.date.november'),
        Messages('app.date.december')
    ][month];
};
        

/**
 * Parameters: the date in milliseconds since epoch
 * whether to include the time, or just the date (default: include time)
 */
Handlebars.registerHelper(
    'dateRender',
    function (date, includeTime) {
        if (date == 0 || date == null)
            return '-';

        if (typeof(includeTime) == 'undefined' || includeTime == null) {
            var includeTime = true;
        }

        var d = new Date(date);
        
        // TODO: time zone?
        if (includeTime) {
            return d.getDate() + ' ' + getMonth(d.getMonth()) + ' ' + d.getFullYear() + ' ' + d.getHours() + ':' + 
                (d.getMinutes() < 10 ? '0' + d.getMinutes() : d.getMinutes());
        }
        else {
            return d.getDate() + ' ' + getMonth(d.getMonth()) + ' ' + d.getFullYear();
        }
    }
);
            
            
