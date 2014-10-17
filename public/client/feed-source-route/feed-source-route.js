var FeedSource = require('feed-source');
var FeedSourceView = require('feed-source-view');
var app = require('application');

module.exports = function (feedSourceId) {
    var model = new FeedSource({id: feedSourceId});
    model.fetch().done(function () {
        app.appRegion.show(new FeedSourceView({model: model}));
    });
};
