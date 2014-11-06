var _ = require('underscore');
var Backbone = require('Backbone');

module.exports = Backbone.Model.extend({
    defaults: {
      feedCollection: null,
      feedVersions: null,
      osmFileId: null,
      otpCommit: null,
      deployedTo: null
    },
    urlRoot: 'api/deployments/'
});
