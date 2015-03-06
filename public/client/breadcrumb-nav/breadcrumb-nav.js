// Breadcrumb nav for the site

var BB = require('bb');
var _ = require('underscore');
var Handlebars = require('handlebars');

// this is just a backbone view; we don't need the machinery of models here
module.exports = BB.View.extend({
  template: Handlebars.compile(require('./breadcrumb-nav.html')),
  tagName: 'ol',
  className: 'breadcrumb',

  /**
   * call this with a list (representing hierarchy) of hashes with href and name
   */
  setLocation: function(location) {
    // home is implied
    var mod = [{
      name: Messages('app.location.home'),
      href: '#'
    }];

    _.each(location, function(val) {
      mod.push(val);
    });

    if (mod.length < 2 || mod[1].href === '#login')
      this.$el.addClass('hidden');
    else
      this.$el.removeClass('hidden');

    this.$el.empty().append(this.template({
      location: mod
    }));

    $('head > title').text(Messages('app.title', mod[mod.length - 1].name));
  }
});
