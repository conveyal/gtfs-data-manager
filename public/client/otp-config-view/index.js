var ItemView = require('item-view');

module.exports = ItemView.extend({
  className: 'row',
  tagName: 'form',
  template: require('./template.html'),

  events: {
    'click .add-updater': 'addUpdater',
    'click .remove-updater': 'removeUpdater',
    'click .save': 'save',
    'submit': 'save'
  },

  initialize: function() {
    var self = this;
    this.listenTo(this.model, 'change:otpConfig', function() {
      self.render();
    });
  },

  serializeData: function() {
    var conf = this.model.get('otpConfig');

    return {
      build: dataToInputs(conf.build),
      router: dataToInputs(conf.router),
      updaters: (conf.updaters || []).map(dataToInputs)
    };
  },

  addUpdater: function(e) {
    e.preventDefault();
    var data = this.serializeForm();
    data.updaters.push({
      type: '',
      sourceType: '',
      url: '',
      frequency: -1,
      defaultAgencyId: ''
    });
    this.model.set('otpConfig', data);
  },

  removeUpdater: function(e) {
    e.preventDefault();
    this.$(e.target).closest('fieldset[data-section=updater]').remove()
    this.model.set('otpConfig', this.serializeForm());
  },

  serializeForm: function() {
    return {
      build: inputsToData(this.$('fieldset[data-section=build]')),
      router: inputsToData(this.$('fieldset[data-section=router]')),
      updaters: this.$('fieldset[data-section=updater]').get().map(inputsToData)
    };
  },

  save: function(e) {
    e.preventDefault();
    this.model.set('otpConfig', this.serializeForm());
  }
});

function dataToInputs(data) {
  if (!data) return [];
  var inputs = [];
  for (var key in data) {
    inputs.push({
      name: key,
      value: data[key],
      type: 'text',
      placeholder: key
    });
  }
  return inputs;
}

function inputsToData(el) {
  var data = {};
  $(el).find('input').each(function() {
    data[$(this).attr('name')] = parse($(this).val());
  });
  return data;
}

function parse(v) {
  try {
    var f = parseFloat(v);
    if ((f + '').length === v.length) return f;
  } catch(e) {}

  try {
    var i = parseInt(v);
    if ((i + '').length === v.length || (i + '.0').length === v.length) return i;
  } catch(e) {}

  if (v === 'true' || v === 'false') return v === 'true';
  return v;
}
