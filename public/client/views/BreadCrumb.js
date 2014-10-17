// Breadcrumb nav for the site

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var m = require('models');
var Handlebars = require('handlebars');

// this is just a backbone view; we don't need the machinery of models here
module.exports.BreadCrumb = Backbone.View.extend({
    template: Handlebars.compile(require('./BreadCrumb.html')),
    tagName: 'ol',
    className: 'breadcrumb',
    
    /**
     * call this with a list (representing hierarchy) of hashes with href and name
     */
    setLocation: function (location) {
        // home is implied
        location.splice(0, 0, {name: Messages('app.location.home'), href: '#'});

        this.$el.empty().append(this.template({location: location}));
    }
}); 
            
