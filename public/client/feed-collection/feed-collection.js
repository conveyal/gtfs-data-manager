var BB = require('bb');

module.exports = BB.Model.extend({
  defaults: {
    name: null,
    id: null,
    user: null,
    useCustomOsmBounds: null,
    osmWest: null,
    osmSouth: null,
    osmEast: null,
    osmNorth: null,
    buildConfig: null,
    routerConfig: null,
    defaultTimeZone: null,
    defaultLanguage: null,
    defaultLocationLat: null,
    defaultLocationLon: null
  },
  urlRoot: 'api/feedcollections/'
});
