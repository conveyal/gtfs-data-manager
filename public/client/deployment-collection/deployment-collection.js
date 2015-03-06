var BB = require('bb');
var Deployment = require('deployment');

module.exports = BB.Collection.extend({
    model: Deployment,
    url: 'api/deployments',
    // reverse-sort by date created
    comparator: function (d) { return -d.get('dateCreated'); }
});
