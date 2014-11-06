var Backbone = require('backbone');
Backbone.Marionette = require('backbone.marionette');
var _ = require('underscore');
var Handlebars = require('handlebars');
var $ = require('jquery');

module.exports = Backbone.Marionette.ItemView.extend({
      template: Handlebars.compile(require('./deployment-progress-view.html')),

      initialize: function(attr) {
        this.model = new Backbone.Model({
          name: attr.name,
          target: attr.target
        });

        _.bindAll(this, 'poll');
      },

      /**
       * receive the current status of the deployment, and update dialog accordingly.
       */
      poll: function(data) {
        if (data.completed) {
          this.$('button.close').removeClass('hidden');
          this.$('.progress-bar').removeClass('active').removeClass('progress-bar-striped');
          // no need to keep polling
          clearInterval(this.interval);

          if (!data.error) {
            // yay, it finished successfully!
            this.setMessage('app.deployment.success');
            this.setBarPosition(100);
            this.$('.progress-bar').addClass('progress-bar-success');
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
            // are we uploading or waiting for the server to build a graph?
            if (data.uploading)
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
