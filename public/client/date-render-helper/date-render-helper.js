// a helper to render a date in milliseconds since epoch as something a normal
// human can understand

var Handlebars = require('handlebars');

// get the month in the local language
var getMonth = function (month) {
    return [
        window.Messages('app.date.january'),
        window.Messages('app.date.february'),
        window.Messages('app.date.march'),
        window.Messages('app.date.april'),
        window.Messages('app.date.may'),
        window.Messages('app.date.june'),
        window.Messages('app.date.july'),
        window.Messages('app.date.august'),
        window.Messages('app.date.september'),
        window.Messages('app.date.october'),
        window.Messages('app.date.november'),
        window.Messages('app.date.december')
    ][month];
};
        

/**
 * Parameters: the date in milliseconds since epoch
 * whether to include the time, or just the date (default: include time)
 */
Handlebars.registerHelper(
    'dateRender',
    function (date, includeTime) {
        if (date === 0 || date === null)
            return '-';

        if (typeof(includeTime) == 'undefined' || includeTime === null) {
            includeTime = true;
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
            
            
