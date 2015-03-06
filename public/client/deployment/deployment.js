var BB = require('bb');

module.exports = BB.Model.extend({
  defaults: {
    feedCollection: null,
    feedVersions: null,
    osmFileId: null,
    otpCommit: null,
    deployedTo: null
  },
  urlRoot: 'api/deployments/'
});
