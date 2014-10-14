var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars.js');
var app = require('application');
var v = require('views');
var m = require('models');

module.exports = function () {
    var Admin = Backbone.Marionette.LayoutView.extend({
        regions: {collectionRegion: '#collection'},
        template: Handlebars.compile(require('./admin.html')),
        onShow: function () {
            var agencies = new m.FeedCollectionCollection();
            var instance = this;
            agencies.fetch().done(function () {
                instance.collectionRegion.show(new v.FeedCollectionCollectionView({collection: agencies}));
            })
        }
    });
    
    // show your work
    app.appRegion.show(new Admin());
}
