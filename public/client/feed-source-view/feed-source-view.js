/**
 * Show some information about a feed source
 */

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');
var FeedVersion = require('feed-version');
var FeedVersionCollection = require('feed-version-collection');
var FeedSource = require('feed-source');
var FeedVersionView = require('feed-version-view');
var FeedUploadView = require('feed-upload-view');
var FeedVersionCollectionView = require('feed-version-collection-view');

module.exports = Backbone.Marionette.LayoutView.extend({
    template: Handlebars.compile(require("./feed-source-view.html")),
    regions: {validationRegion: '#validation'},

    events: {
        'click .upload-feed': 'uploadFeed',
        'click #share-url': 'doNothing',
        'click .show-all-versions': 'showAllVersions'
    },
    initialize: function (attr) {
        this.feedVersionId = attr.feedVersionId;

        _.bindAll(this, 'uploadFeed', 'showAllVersions');
    },

    // show the feed upload dialog
    uploadFeed: function (e) {
        // model is so that it knows what feed source to upload to
        app.modalRegion.show(new FeedUploadView({model: this.model}));
    },

    // show all of the versions of this feed
    showAllVersions: function () {
        // get the data
        var versions = new FeedVersionCollection();
        var instance = this;
        versions.fetch({data: {feedsource: this.model.get('id')}}).done(function () {
            // we don't need to set up any events to return to the validation view, as that will be handled
            // through a URL hash change and the router
            instance.validationRegion.show(new FeedVersionCollectionView({collection: versions}));
        });
    },
    
    onShow: function () {
        var version;

        if (this.feedVersionId != undefined && this.feedVersionId != null)
            version = new FeedVersion({id: this.feedVersionId});

        else
            version = new FeedVersion({id: this.model.get('latestVersionId')});

        var instance = this;
        version.fetch().done(function () {
            instance.validationRegion.show(new FeedVersionView({model: version}));
        });

        // expose the copypastable URL to allow users to view/edit
        if (app.user.admin) {
            var instance = this;
            $.ajax({
                url: 'api/feedsources/' + this.model.get('id') + '/getKey',
                success: function (data) {
                    instance.$('#share-url').val(window.location.origin + window.location.pathname + window.location.hash +
                                                 '?userId=' + encodeURIComponent(data['userId']) + '&key=' + encodeURIComponent(data['key']));
                }
            });
        }

        // set up nav
        app.nav.setLocation([
            {name: this.model.get('feedCollection').name, href: '#overview/' + this.model.get('feedCollection').id},
            {name: this.model.get('name'), href: '#feed/' + this.model.get('id')},
        ]);
    },


    // don't bubble clicks in the input field (e.g. to copy)
    doNothing: function (e) {
        e.preventDefault();
        e.stopPropagation();
    }

})
