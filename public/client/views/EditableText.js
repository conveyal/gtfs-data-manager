// represents an editable attribute of a model
// each of these gets its own reference to the model, which is fine
// Backbone will handle all the concurrent updates

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var m = require('models');
var Handlebars = require('handlebars');

module.exports.EditableTextView = Backbone.Marionette.ItemView.extend({
    template: Handlebars.compile(require('./EditableText.html')),
    tagName: 'span',
    events: { 
        'click .toggle-edit': 'edit'
    },

    initialize: function () {
        _.bindAll(this, 'edit');
        // keep track of whether the field is currently being edited or not
        this.editing = false;
    },

    // toggle editing of the field
    edit: function (e) {
        this.$('.input').toggleClass('hidden');

        this.$('.glyphicon').toggleClass('glyphicon-pencil').toggleClass('glyphicon-ok');

        if (this.editing) {
            // save
            var attr = [];
            attr[this.attribute] = this.$('input').val();
            this.model.set(attr);
            this.model.save();

            this.$('.label').text(Messages('app.save'));
            this.render();
        }
        else {
            this.$('.label').text(Messages('app.edit'));
        }
        
        this.editing = !this.editing;
    },

    // we override serializeDate because we need to get the text of a particular field, and the
    // view doesn't know which field that is
    serializeData: function () {
        return {text: this.model.get(this.attribute), href: this.href || '#'};
    }
});

    