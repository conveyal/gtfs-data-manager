// a helper to render a date in milliseconds since epoch as something a normal
// human can understand

var Handlebars = require('handlebars');
var moment = require('moment');

/**
 * Parameters: the date in milliseconds since epoch
 * whether to include the time, or just the date (default: include time)
 */
Handlebars.registerHelper(
  'dateRender',
  function(date, includeTime) {
    if (date === 0 || date === null)
      return '-';

    var m = moment(date);

    // TODO: time zone?
    return new Handlebars.SafeString(
      '<span>' +
      Handlebars.escapeExpression(m.format(window.Messages('app.date_format'))) + ' (' +
      Handlebars.escapeExpression(m.fromNow()) +
      ')</span>');
  }
);
