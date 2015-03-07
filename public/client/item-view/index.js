var BB = require('bb');
var Templater = require('templater');

module.exports = BB.Marionette.ItemView.extend({
  onBeforeRender: function() {
    if (this.template && typeof this.template === 'string')
      this.template = Templater.compile(this.template);
  }
});
