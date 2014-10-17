/*
 * Upload a feed to the manager manually.
 */

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var m = require('models');
var Handlebars = require('handlebars');

module.exports.FeedUpload = Backbone.Marionette.ItemView.extend({
    template: Handlebars.compile(require('./feed-upload-view.html')),
    onShow: function () {
        this.$('.modal').modal();
    }    
});
