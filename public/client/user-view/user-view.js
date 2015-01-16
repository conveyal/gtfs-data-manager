var Backbone = require('Backbone');
Backbone.Marionette = require('backbone.marionette');
var Handlebars = require('handlebars.js');
var app = require('application');
var _ = require('underscore');
var $ = require('jquery');
jQuery = $;
require('select2');
var FeedSourceCollection = require('feed-source-collection');

module.exports = Backbone.Marionette.ItemView.extend({
  template: Handlebars.compile(require('./user-view.html')),
  events: {
    'keyup .password': 'validatePassword',
    'submit form': 'saveChanges'
  },

  validatePassword: function() {
    var pass = this.$('#password').val();
    var pass2 = this.$('#retype-password').val();

    // require the current user to enter their password to change a password, but not to create a password
    // for a new user
    if (pass.length === pass2.length && pass.length === 0 || !this.model.id)
      this.$('#current-password-group').addClass('hidden');
    else
      this.$('#current-password-group').removeClass('hidden');

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

  onShow: function () {
    var instance = this;

    // initialize the select box, if the user is an admin
    if (app.user.admin) {
      var feedSources = new FeedSourceCollection();

      var userFeedSources = _.pluck(this.model.get('projectPermissions'), 'project_id');

      feedSources.fetch().done(function() {
        feedSources.each(function(feedSource) {
          var og = instance.$('optgroup#' + feedSource.get('feedCollection').id);

          if (og.length === 0) {
            og = $('<optgroup>')
              .attr('id', feedSource.get('feedCollection').id)
              .attr('label', feedSource.get('feedCollection').name);
            og.appendTo(instance.$('#feedsources'));
          }

          var opt = $('<option>')
            .attr('value', feedSource.id)
            .text(feedSource.get('name'));

          if (userFeedSources.indexOf(feedSource.id) !== -1) {
            opt.attr('selected', true);
          }

          opt.appendTo(og);

        });

        instance.$('#feedsources').select2();
      });
    }
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

    var projectPermissions = [];

    this.model.set('projectPermissions', projectPermissions);

    var pass = this.$('#password').val();

    if (pass !== '') {
      this.model.set('password', pass);
      // we pass along the current user password as well if we are doing an update. the user must authenticate to change passwords.
      if (this.model.id)
        this.model.set('currentUserPassword', this.$('#current-password').val());
    }

    if (app.user.admin) {
      _.each(this.$('#feedsources').val(), function (fsid) {
        projectPermissions.push({
          project_id: fsid,
          read: true,
          write: true,
          admin: false // not used
        });
      });

      this.model.set({
        admin: this.$('#admin').is(':checked'),
        active: this.$('#active').is(':checked')
      });
    }

    return true;
  },

  saveChanges: function () {
    var instance = this;

    this.$('#error').addClass('hidden');

    if (!this.validatePassword())
      return false;

    if (!this.updateModel())
      return false;

    var id = this.model.id;

    // if it's a new user, send them back to the user list
    this.model.save().done(function () {
      if (!id) {
        window.location.hash = '#users';
      }
    })
    .fail(function () {
      instance.$('#error').removeClass('hidden');
    });

    // no need to keep these floating around on the client
    this.model.set('password', null);
    this.model.set('currentUserPassword', null);

    // use the class, clear both fields
    this.$('.password').val('');

    // always return false so that the form is not submitted
    return false;
  }
});
