var BB = require('bb');
var _ = require('underscore');
var Handlebars = require('handlebars');

module.exports = BB.Marionette.ItemView.extend({
      template: Handlebars.compile(require('./deployment-progress-view.html')),

      initialize: function(attr) {
        this.model = new BB.Model({
          name: attr.deployment.get('name'),
          target: attr.target
        });

        this.deployment = attr.deployment;

        _.bindAll(this, 'poll');
      },

      /**
       * receive the current status of the deployment, and update dialog accordingly.
       */
      poll: function(data) {
        if (data.completed) {
          this.$('.progress-bar').removeClass('active').removeClass('progress-bar-striped');
          // no need to keep polling
          clearInterval(this.interval);

          if (!data.error) {
            // yay, it finished successfully!
            this.setMessage('app.deployment.success');
            this.setBarPosition(100);
            this.$('.progress-bar').addClass('progress-bar-success');

            // set up the link to view it
            var href = data.baseUrl;

            if(href) {
              if (href.slice(-1) != '/')
                href += '/';

              // calculate the centroid latitude and longitude
              var bounds = this.deployment.get('bounds');
              var lat = (bounds.north + bounds.south) / 2;
              var lon = (bounds.east + bounds.west) / 2;

              // figure out the zoom. assume that otp.js will open in a window of the same size (e.g. a new tab)
              var width = $(window).width();
              var height = $(window).height();

              // what fraction of the world is this from north to south?
              // note that we are storing the denominator only, to avoid roundoff errors
              var boundsHeightMerc = 180 / (bounds.north - bounds.south);

              // longitude is generally more complicated, because the length depends on the latitude
              // however, because we're using a Mercator projection, the map doesn't understand this either,
              // and maps 360 degrees of longitude to an invariant width
              // This is why Greenland appears larger than Africa, but it does make the math easy.
              var boundsWidthMerc = 360 / (bounds.east - bounds.west);

              // figure out the zoom level
              // level 0 is the entireer world in a single 256x256 tile, next level
              // is entire world in 256 * 2^1, then 256 * 2^2, and so on
              var z = 23;

              while (true) {
                var worldSize = 256 * Math.pow(2, z);
                // again, store the denominator/reciprocal
                var windowWidthMerc = worldSize / width;
                var windowHeightMerc = worldSize / height;

                // if it fits. We use < not > because we have stored the reciprocals.
                if (windowWidthMerc < boundsWidthMerc && windowHeightMerc < boundsHeightMerc || z === 0)
                  break;

                z--;
              }

              var routerId = this.deployment.get('routerId');
              href += '#start/' + lat + '/' + lon + '/' + z + '/' +
                (!_.isUndefined(routerId) && routerId !== null ? routerId : 'default');

              this.$('#result-link').attr('href', href)
                .removeClass('hidden');
            }
          } else {
            // uh-oh
            this.setMessage(data.message || 'app.deployment.error');
            this.$('.progress-bar').addClass('progress-bar-danger');
          }
        }
        else {
          // deployment in progress
          if (!data.built) {
            // the server is building the data bundle
            this.setMessage('app.deployment.building_bundle');
          }
          else {
            // are we uploading to S3, OTP, or waiting for the server to build a graph?
            if(data.uploadingS3) {
              this.setMessage('app.deployment.uploading_s3');
              this.setBarPosition(data.percentUploaded);
            }
            else if (data.uploading)
              this.setMessage('app.deployment.uploading', data.numServersCompleted + 1, data.totalServers);
            else
              this.setMessage('app.deployment.building_graph', data.numServersCompleted + 1, data.totalServers);

            // figure out progress bar position
            var barPosition = 10; // we give 10% for building the bundle, which is done now

            // figure out how many percent each server is worth
            var serverPct = 90 / data.totalServers;

            // add the amounts for each server that has completed so far
            barPosition += serverPct * data.numServersCompleted;

            // if we've finished uploading and are waiting for graph build on this server, give it another half
            if (!data.uploading)
              barPosition += serverPct / 2;

            this.setBarPosition(barPosition);
          }
        }
      },

      /**
       * Convenience function to set progress bar position
       */
      setBarPosition: function(percent) {
        this.$('.progress-bar')
          .css('width', percent + '%')
          .attr('aria-valuenow', percent);
      },

      /**
       * Convenience function to set status readout
       */
      setMessage: function() {
        $('#progress-message').text(window.Messages.apply(window, arguments));
      },

      onShow: function () {
        this.$('.modal').modal();

        var instance = this;
        this.interval = setInterval(function () {
          $.ajax({
            url: 'api/deployments/status/' + encodeURIComponent(instance.model.get('target')),
            success: instance.poll
          });
        }, 1000);
      },

      onBeforeDestroy: function () {
        clearInterval(this.interval);
      }
    });
