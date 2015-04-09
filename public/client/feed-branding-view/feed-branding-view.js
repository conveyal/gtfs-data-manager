var ItemView = require('item-view');
var FeedVersion = require('feed-version');
var _ = require('underscore');

var Handlebars = require('handlebars');

module.exports = ItemView.extend({
  template: require('./feed-branding-view.html'),

  events: {
      'click .upload-button': 'upload',
      'submit form': 'onSubmit'
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
        return options.inverse(this);;
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

  onSubmit: function(evt) {
    var filename = evt.target[0].value;
    if(!filename || filename.toLowerCase().indexOf('.png', filename.length - 4) === -1) {
      alert('Must be a PNG file');
      evt.preventDefault();
    }
  }

 });