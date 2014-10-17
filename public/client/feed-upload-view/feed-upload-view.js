/*
 * Upload a feed to the manager manually.
 */

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars');

module.exports = Backbone.Marionette.ItemView.extend({
    template: Handlebars.compile(require('./feed-upload-view.html')),
    onShow: function () {
        this.$('.modal').modal();
    }    
});
