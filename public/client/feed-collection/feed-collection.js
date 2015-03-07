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
    otpConfig: {
      build: {
        fetchElevationUS: true,
        stationTransfers: true,
        subwayAccessTime: 2.5
      },
      router: {
        numItineraries: 6,
        walkSpeed: 2.0,
        stairsReluctance: 4.0,
        carDropoffTime: 240
      },
      updaters: []
    }
  },
  urlRoot: 'api/feedcollections/'
});
