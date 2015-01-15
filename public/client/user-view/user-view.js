var Backbone = require('Backbone');
Backbone.Marionette = require('backbone.marionette');
var Handlebars = require('handlebars.js');
var app = require('application');
var _ = require('underscore');

module.exports = Backbone.Marionette.ItemView.extend({
  template: Handlebars.compile(require('./user-view.html')),
  events: {
    'keyup .password': 'validatePassword',
    'submit form': 'saveChanges'
  },
  validatePassword: function() {
    var pass = this.$('#password').val();
    var pass2 = this.$('#retype-password').val();

    if (pass != pass2) {
      this.$('#retype-password').parent().addClass('has-error')
      this.$('#retype-password').attr('aria-invalid', 'true')
        .attr('aria-describedby', 'dont-match');
      this.$('#dont-match').removeClass('hidden');
      return false;
    } else {
      this.$('#retype-password').parent().removeClass('has-error')
      this.$('#retype-password').attr('aria-invalid', false)
        .attr('aria-describedby', false);
      this.$('#dont-match').addClass('hidden');
      return true;
    }
  },

  initialize: function() {
    _.bindAll(this, 'validatePassword', 'saveChanges');
  },

  serializeData: function() {
    // include the current user so that the view can figure out what is fair game for editing
    var ret = {
      currentUser: app.user
    };
    return Object.assign(ret, this.model.toJSON());
  },

  updateModel: function() {
    // only set the username on a new user
    if (!this.model.id) {
      var username = this.$('#username').val();
      if (username)
        this.model.set('username', username);
      else
        // TODO: alert what went wrong
        return false;
    }

    this.model.set('email', this.$('#email').val());

    var pass = this.$('#password').val();

    if (pass !== '') {
      this.model.set('password', pass);
    }

    if (app.user.admin) {
      this.model.set({
        admin: this.$('#admin').is(':checked'),
        active: this.$('#active').is(':checked')
      });
    }

    return true;
  },

  saveChanges: function () {
    if (!this.validatePassword())
      return false;

    if (!this.updateModel())
      return false;

    var id = this.model.id;

    // if it's a new user, send them back to the user list
    this.model.save().done(function () {
      if (id) {
        window.location.hash = '#users';
      }
    })

    this.$('.password').val('');

    // always return false so that the form is not submitted
    return false;
  }
});
