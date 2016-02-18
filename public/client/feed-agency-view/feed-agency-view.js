var ItemView = require('item-view');
var FeedVersion = require('feed-version');
var _ = require('underscore');
var app = require('application');

var UpdateLogoDialog = require('./update-logo');

var Handlebars = require('handlebars');

module.exports = ItemView.extend({
  template: require('./feed-agency-view.html'),

  events: {
      'click .update-logo-button': 'updateLogo'
  },

  initialize: function(attr) {
    var self = this;

    if (_.isUndefined(this.model)) {
      // we create a dummy model simply so we don't have to check in the view if the model exists, only if its properties exist
      // and so we can access the feedSource in a uniform way regardless of whether the version exists
      this.model = new FeedVersion({
        feedSource: attr.feedSource.toJSON()
      });
    }

    Handlebars.registerHelper(
      'hasAgencyLogo',
      function(agencyId, options) {
        var branding = self.getAgencyBranding(agencyId);
        if(branding !== null && branding.hasLogo) return options.fn(this);
        return options.inverse(this);
      }
    );

    Handlebars.registerHelper(
      'getAgencyLogoUrl',
      function(agencyId, options) {
        var branding = self.getAgencyBranding(agencyId);
        if(branding !== null && branding.hasLogo) {
          return branding.urlRoot + '/' + encodeURIComponent(agencyId) + '/logo.png?' + Date.now();
        }
      }
    );

    Handlebars.registerHelper(
      'canManageBranding',
      function(agencyId, options) {
        var fs = self.model.get('feedSource');
        var canManageFeed = app.auth0User.canManageFeed(fs.feedCollection.id, fs.id);
        if(canManageFeed) return options.fn(this);
        return options.inverse(this);
      }
    );
  },

  getAgencyBranding: function(agencyId) {
    var retval = null;
    _.each(this.model.get('feedSource').branding, function(agencyBranding) {
      if(agencyBranding.agencyId == agencyId) {
        retval = agencyBranding;
      }
    });
    return retval;
  },

  updateLogo: function(evt) {
    //var dialogTemplate = Handlebars.compile(require('./update-branding.html'));

    var agencyId = $(evt.target).data('agencyId');
    var view = this;

    app.modalRegion.show(new UpdateLogoDialog ({
      agencyId : agencyId,
      feedSourceId : view.model.get('feedSource').id
    }));
  }

 });
