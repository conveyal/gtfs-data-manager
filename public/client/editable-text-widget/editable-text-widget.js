// represents an editable attribute of a model
// each of these gets its own reference to the model, which is fine
// Backbone will handle all the concurrent updates

var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var $ = require('jquery');
var _ = require('underscore');
var Handlebars = require('handlebars');

module.exports = Backbone.Marionette.ItemView.extend({
    template: Handlebars.compile(require('./editable-text-widget.html')),
    tagName: 'span',
    className: 'EditableText',
    events: {
        'click .toggle-edit': 'edit'
    },

    initialize: function (attr) {
        this.attribute = attr.attribute || this.attribute;
        this.maxWidth = attr.maxWidth;
        this.href = attr.href || this.href;

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
            var attr = {};
            attr[this.attribute] = this.$('input').val();
            this.model.set(attr);

            var instance = this;
            this.model.save().done(function () {
                instance.render();
            });

            this.$('.label').text(Messages('app.save'));
        }
        else {
            this.$('.label').text(Messages('app.edit'));
            this.$('input').focus();
        }

        this.editing = !this.editing;
    },

    // we override serializeData because we need to get the text of a particular field, and the
    // view doesn't know which field that is
    serializeData: function () {
        var href;

        if (typeof this.href == "function")
            href = this.href();

        else if (_.isUndefined(this.href))
            href = '#';

        else
            href = this.href;

        var text = this.model.get(this.attribute);

        if (!_.isUndefined(this.maxWidth) && !_.isNull(this.maxWidth) && !_.isNull(text) && text.length >= this.maxWidth) {
          text = text.slice(0, this.maxWidth - 1);
          text += '…';
        }

        var value = this.model.isNew() ? '' : this.model.get(this.attribute);

        return {
          displayText: text,
          value: value,
          placeholder: text,
          href: href
        };
    }
});
