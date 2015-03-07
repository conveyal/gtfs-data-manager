// A list of FeedVersions for a particular FeedSource

var app = require('application');
var CompositeView = require('composite-view');
var FeedSource = require('feed-source');
var ItemView = require('item-view');

var FeedVersionItemView = ItemView.extend({
  template: require('./feed-version-item-view.html'),
  tagName: 'tr'
});

module.exports = CompositeView.extend({
  childView: FeedVersionItemView,
  childViewContainer: 'tbody',
  template: require('./feed-version-collection-view.html'),

  initialize: function() {
    // extract the feed source
    this.model = new FeedSource(this.collection.at(0).get('feedSource'));
  },

  onShow: function() {
    app.nav.setLocation([{
      name: this.collection.at(0).get('feedSource').feedCollection.name,
      href: '#overview/' + this.collection.at(0).get('feedSource').feedCollection.id
    }, {
      name: this.collection.at(0).get('feedSource').name,
      href: '#feed/' + this.collection.at(0).get('feedSource').id
    }, {
      name: window.Messages('app.feed_version.versions'),
      href: '#versions/' + this.collection.at(0).get('feedSource').id
    }]);
  }
});
