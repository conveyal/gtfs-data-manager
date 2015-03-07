var BB = require('bb');
var Templater = require('templater');

module.exports = BB.Marionette.LayoutView.extend({
  onBeforeRender: function() {
    if (this.template && typeof this.template === 'string')
      this.template = Templater.compile(this.template);
  }
});
