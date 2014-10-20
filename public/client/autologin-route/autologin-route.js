/**
 * Automatically log in a user with a userId and key, so they can view/upload to a feedsource.
 */

var app = require('application');

module.exports = function (feedSourceId, userId, key) {
    $.post('/authenticate',
           {
               userId: userId,
               key: key
           })
        .then(function (data) {
            $('#logged-in-user').text(window.Messages('app.account.logged_in_as', data['username']));
            $('#logout').removeClass('hidden');
            
            // note: log out is handled in application.js

            app.user = data;
            
            window.location.hash = '#feed/' + feedSourceId;
        })
        .fail(function () {
            // TODO: alert is bad, bad error message, not translatable.
            alert('Invalid key');
        });
}

        
