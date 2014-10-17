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
var FeedSource = require('feed-source');
var FeedVersionView = require('feed-version-view');
var FeedUploadView = require('feed-upload-view');

module.exports = Backbone.Marionette.LayoutView.extend({
    template: Handlebars.compile(require("./feed-source-view.html")),
    regions: {latestValidationRegion: '#latest-validation'},

    events: {'click .upload-feed': 'uploadFeed' },
    initialize: function () {
        _.bindAll(this, 'uploadFeed');
    },

    // show the feed upload dialog
    uploadFeed: function (e) {
        // model is so that it knows what feed source to upload to
        app.modalRegion.show(new FeedUploadView({model: this.model}));
    },
    
    onShow: function () {
        var latest = new FeedVersion({id: this.model.get('latestVersionId')});
        var instance = this;
        latest.fetch().done(function () {
            instance.latestValidationRegion.show(new FeedVersionView({model: latest}));
        });

        // set up nav
        app.nav.setLocation([
            {name: this.model.get('feedCollection').name, href: '#overview/' + this.model.get('feedCollection').id},
            {name: this.model.get('name'), href: '#feed/' + this.model.get('id')},
        ]);
    }
})
