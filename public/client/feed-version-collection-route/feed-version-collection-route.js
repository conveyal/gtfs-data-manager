var FeedVersionCollection = require('feed-version-collection');
var FeedVersionCollectionView = require('feed-version-collection-view');
var app = require('application');

module.exports = function (feedSourceId) {
    // get the data
    var versions = new FeedVersionCollection();
    var instance = this;
    versions.fetch({data: {feedsource: feedSourceId}}).done(function () {
        app.appRegion.show(new FeedVersionCollectionView({collection: versions}));
    });
};
