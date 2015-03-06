var BB = require('bb');
var Handlebars = require('handlebars.js');
var app = require('application');
var FeedCollection = require('feed-collection');

module.exports = BB.Marionette.ItemView.extend({
  template: Handlebars.compile(require('./osm-config-view.html')),

  events: {
    'click #save-button': 'saveChanges',
    'click #use-gtfs': 'useGtfsClicked',
    'click #use-custom': 'useCustomClicked'
  },

  onShow: function() {
    if (this.model.get('useCustomOsmBounds')) {
      $('#use-custom').prop('checked', true);
    } else {
      $('#use-gtfs').prop('checked', true);
      $('#custom-inputs').hide();

      var north, south, east, west;
      this.options.feedSources.each(function(feedSource) {
        var latestVal = feedSource.get('latestValidation');
        if (!latestVal || !latestVal.bounds) return;
        var bounds = latestVal.bounds;
        west = west ? Math.min(west, bounds.west) : bounds.west;
        south = south ? Math.min(south, bounds.south) : bounds.south;
        east = east ? Math.max(east, bounds.east) : bounds.east;
        north = north ? Math.max(north, bounds.north) : bounds.north;
      });

      $('#osmWest').val(west || 0);
      $('#osmSouth').val(south || 0);
      $('#osmEast').val(east || 0);
      $('#osmNorth').val(north || 0);

    }
  },

  useGtfsClicked: function() {
    $('#custom-inputs').slideUp();
  },

  useCustomClicked: function() {
    $('#custom-inputs').slideDown();
  },

  saveChanges: function() {
    var useCustom = ($('input:radio[name=useCustom]:checked').val() === 'true');

    if (!useCustom) {
      this.model.set('useCustomOsmBounds', false);
    } else {
      var west = this.$('#osmWest').val();
      if (!isNumber(west)) {
        window.alert("West bound is not valid");
        return;
      }

      var south = this.$('#osmSouth').val();
      if (!isNumber(south)) {
        window.alert("South bound is not valid");
        return;
      }

      var east = this.$('#osmEast').val();
      if (!isNumber(east)) {
        window.alert("East bound is not valid");
        return;
      }

      var north = this.$('#osmNorth').val();
      if (!isNumber(north)) {
        window.alert("North bound is not valid");
        return;
      }

      this.model.set('useCustomOsmBounds', true);
      this.model.set('osmWest', west);
      this.model.set('osmSouth', south);
      this.model.set('osmEast', east);
      this.model.set('osmNorth', north);
    }

    var self = this;

    this.model.save().done(function() {
        window.location.hash = '#overview/' + self.model.get('id');
      })
      .fail(function() {
        window.alert('Error saving OSM settings');
      });
  }

});

function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}
