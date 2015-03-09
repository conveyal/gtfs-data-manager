var ItemView = require('item-view');
var _ = require('underscore');

module.exports = ItemView.extend({
  className: 'row',
  tagName: 'form',
  template: require('./template.html'),

  events: {
    'click .add-updater': 'addUpdater',
    'click .remove-updater': 'removeUpdater',
    'submit': 'save'
  },

  initialize: function() {
    var self = this;
    this.listenTo(this.model, 'change:routerConfig', function() {
      self.render();
    });
  },

  serializeData: function() {
    var buildConfig = this.model.get('buildConfig');
    var routerConfig = this.model.get('routerConfig');

    return {
      build: dataToInputs(buildConfig),
      router: dataToInputs(_.omit(routerConfig, 'updaters')),
      updaters: (routerConfig ? (routerConfig.updaters || []) : []).map(dataToInputs)
    };

    /*var conf = this.model.get('otpConfig');

    return {
      build: dataToInputs(conf.build),
      router: dataToInputs(conf.router),
      updaters: (conf.updaters || []).map(dataToInputs)
    };*/
  },

  addUpdater: function(e) {
    e.preventDefault();
    var data = this.serializeForm();
    data.routerConfig.updaters.push({
      type: null,
      sourceType: null,
      url: null,
      frequency: null,
      defaultAgencyId: null
    });
    this.model.set('routerConfig', data.routerConfig);
  },

  removeUpdater: function(e) {
    e.preventDefault();
    this.$(e.target).closest('fieldset[data-section=updater]').remove()
    this.model.set('otpConfig', this.serializeForm());
  },

  serializeForm: function() {
    var routerConfig = inputsToData(this.$('fieldset[data-section=router]'));
    routerConfig.updaters = this.$('fieldset[data-section=updater]').get().map(inputsToData);
    return {
      buildConfig: inputsToData(this.$('fieldset[data-section=build]')),
      routerConfig: routerConfig
    };
  },

  save: function(e) {
    e.preventDefault();
    this.model.save(this.serializeForm());
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
    if (!f.isNaN() && (f + '').length === v.length) return f;
  } catch(e) {}

  try {
    var i = parseInt(v);
    if (!i.isNaN() && ((i + '').length === v.length || (i + '.0').length === v.length)) return i;
  } catch(e) {}

  if (v === 'true' || v === 'false') return v === 'true';

  if (v === '') return null;

  return v;
}
