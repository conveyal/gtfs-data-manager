var BB = require('bb');

module.exports = BB.Model.extend({
  defaults: {
    name: null,
    isPublic: false,
    deployable: false,
    retrievalMethod: null,
    lastFetched: null,
    lastUpdated: null,
    url: null,
    latest: null,
    feedCollection: null,
    shortName: null,
    AgencyPhone: null,
    RttAgencyName: null,
    RttEnabled: null,
    AgencyShortName: null,
    AgencyPublicId: null,
    AddressLat: null,
    AddressLon: null,
    DefaultRouteType: null,
    CarrierStatus: null,
    AgencyAddress: null,
    AgencyEmail: null,
    AgencyUrl: null,
    AgencyFareUrl: null,
  },
  urlRoot: 'api/feedsources/'
});
