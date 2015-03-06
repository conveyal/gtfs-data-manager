/**
 * Automatically log in a user with a userId and key, so they can view/upload to a feedsource.
 */

var app = require('application');

module.exports = function() {
  var feedSourceId, feedVersionId, userId, key;

  // yes, this is in fact four arguments, not five, but the Bootstrap router throws an extra null on the end
  if (arguments.length == 5) {
    feedSourceId = arguments[0];
    feedVersionId = arguments[1];
    userId = arguments[2];
    key = arguments[3];
  } else if (arguments.length == 4) {
    feedSourceId = arguments[0];
    feedVersionId = null;
    userId = arguments[1];
    key = arguments[2];
  } else return;

  $.post('/authenticate', {
      userId: userId,
      key: key
    })
    .then(function(data) {
      $('#logged-in-user').text(window.Messages('app.account.logged_in_as', data['username']));
      $('#logout').removeClass('hidden');

      // note: log out is handled in application.js

      app.user = data;

      window.location.hash = '#feed/' + feedSourceId +
        (feedVersionId != null ? '/' + feedVersionId : '');
    })
    .fail(function() {
      // TODO: alert is bad, bad error message, not translatable.
      alert('Invalid key');
    });
}
