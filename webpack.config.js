'use strict';

var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  devtool: 'eval-source-map',
  entry: [
    'webpack-hot-middleware/client?reload=true',
    path.join(__dirname, 'public/new_client/app/main.js')
  ],
  resolve: {
    extensions: ['', '.html', '.js', '.json', '.scss', '.css'],
      alias: {
        react_table_css: __dirname + "/node_modules/react-bootstrap-table/css/react-bootstrap-table-all.min.css",
//        leaflet_marker: __dirname + "/node_modules/leaflet/dist/images/marker-icon.png",
//        leaflet_marker_2x: __dirname + "/node_modules/leaflet/dist/images/marker-icon-2x.png",
//        leaflet_marker_shadow: __dirname + "/node_modules/leaflet/dist/images/marker-shadow.png"
    }
  },
  output: {
    path: path.join(__dirname, 'public/new_client/dist/'),
    filename: '[name].js',
    publicPath: '/new_client/dist/'
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: 'public/new_client/app/index.tpl.html',
      inject: 'body',
      filename: 'index.html'
    }),
    new webpack.optimize.OccurenceOrderPlugin(),
    new webpack.HotModuleReplacementPlugin(),
    new webpack.NoErrorsPlugin(),
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify('development')
    })
  ],
  module: {
    loaders: [{
      test: /\.js?$/,
      exclude: /node_modules/,
      loader: 'babel'
    }, {
      test: /\.json?$/,
      loader: 'json'
//    }, {
//      test: /\.css$/,
//      loader: 'style!css?modules&localIdentName=[name]---[local]---[hash:base64:5]'
    }, {
    test: /\.css?$/,
    loader: "style-loader!css-loader!"
    }, {
      test: /\.(png|jpg)$/,
      loader: "file-loader?name=images/[name].[ext]"
    }]
  }
};