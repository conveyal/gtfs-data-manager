// modules/About.js
import React from 'react'
import $ from 'jquery'

import config from '../config'

import moment from 'moment'

import { Link } from 'react-router'
import { Panel, Grid, Row, Col, Button, Table, Glyphicon } from 'react-bootstrap'

import NavigationBar from '../navbar'

export default class Feed extends React.Component {

  constructor(props) {
    super(props)

    this.state = {
      id: this.props.params.feedId,
      feed: {},
      validation: {}
    }
  }
  componentDidMount() {
    this.getFeedById(this.props.params.feedId)
  }

  getFeedById(id) {
    $.ajax({
      // Hard coded feed collection id
      url: '/api/public/feedsources/' + id,
      headers: {
        //        'Authorization': 'Bearer ' + this.props.token
      }
    }).done((data) => {
      console.log(data)
      var feed = JSON.parse(data)

      this.setState({
        feed: feed,
        validation: feed.latestValidation,
      })
      console.log(this.state.feed)
    })
  }

  render() {
    // console.log(this.state.feeds)
    return (
    <div>
      <NavigationBar title={config.title} />
      <Grid>
        <Row>
          <Col xs={12}>
          <h2>{this.state.feed ? this.state.feed.name : this.state.id}</h2>
          <p>
            <Link to="/"> Back to list of feeds
            </Link>
          </p>
          </Col>
        </Row>
        <Row>
          <Col xs={12}>
          <h3>Feed Details</h3>
          <Table>
            <tbody>
              <tr>
                <td className="text-right">
                  <strong>Feed ID</strong>
                </td>
                <td>
                  {this.state.feed.id}
                </td>
              </tr>
              <tr>
                <td className="text-right">
                  <strong>Last Updated</strong>
                </td>
                <td>
                  {this.state.feed.lastUpdated ? moment(this.state.feed.lastUpdated).format('MMMM Do YYYY, h:mm a') : '?'}
                </td>
              </tr>
              <tr>
                <td className="text-right">
                  <strong>Retrieval Method</strong>
                </td>
                <td>
                  {this.state.feed.retrievalMethod}
                </td>
              </tr>
              <tr>
                <td className="text-right">
                  <strong>Link to GTFS</strong>
                </td>
                <td>
                  <a href={this.state.feed.url}>
                    {this.state.feed.url}
                  </a>
                </td>
              </tr>
            </tbody>
          </Table>
          </Col>
        </Row>
        <Row>
          <Col xs={12}>
          <h3>Validation Info</h3>
          <Table>
            <tbody>
              <tr>
                <td className="text-right">
                  <strong>Agencies in Feed</strong>
                </td>
                <td>
                  {typeof this.state.validation.agencies !== 'undefined' ? this.state.validation.agencies.join(', ') : ''}
                </td>
              </tr>
              <tr>
                <td className="text-right">
                  <strong># of Routes</strong>
                </td>
                <td>
                  {this.state.validation.routeCount}
                </td>
              </tr>
              <tr>
                <td className="text-right">
                  <strong># of Stop Times</strong>
                </td>
                <td>
                  {this.state.validation.stopTimesCount}
                </td>
              </tr>
              <tr>
                <td className="text-right">
                  <strong># of Trips</strong>
                </td>
                <td>
                  {this.state.validation.tripCount}
                </td>
              </tr>
              <tr>
                <td className="text-right">
                  <strong>Dates Valid</strong>
                </td>
                <td>
                  {moment(this.state.validation.startDate).format('MMMM Do YYYY') - moment(this.state.validation.endDate).format('MMMM Do YYYY')}
                </td>
              </tr>
              <tr>
                <td className="text-right">
                  <strong>Errors</strong>
                </td>
                <td>
                  {this.state.validation.errorCount}
                </td>
              </tr>
            </tbody>
          </Table>
          </Col>
        </Row>
      </Grid>
    </div>
    )
  }
}
