'use strict';

var request = require('request');


var docs = [
  'book\tCosmos\tCarl Sagan\tScience',
  'journal\tThe Journal of cell biology\tRockefeller University Press',
  'book\tA brief history of time\tStephen W Hawking\tScience',
  'book\tCosmos\tCarl Sagan\tScience',
  'journal\tThe Journal of cell biology\tRockefeller University Press',
  'book\tA brief history of time\tStephen W Hawking\tScience',
  'book\tCosmos\tCarl Sagan\tScience',
  'journal\tThe Journal of cell biology\tRockefeller University Press',
  'book\tA brief history of time\tStephen W Hawking\tScience',
  'book\tCosmos\tCarl Sagan\tScience',
  'journal\tThe Journal of cell biology\tRockefeller University Press',
  'book\tA brief history of time\tStephen W Hawking\tScience',
];

var tickets = [];

//  watermark :: Document -> Promise Ticket
var watermark = function(doc) {
  return new Promise(function(resolve, reject) {
    request({
      method: 'POST',
      url: 'http://localhost:8080/ticket',
      callback: function(err, res, ticket) {
        if (err == null) {
          resolve(ticket);
        } else {
          reject(err);
        }
      }
    });
  });
};

//  retrieve :: Ticket -> Promise Document
var retrieve = function(ticket) {
  return new Promise(function(resolve, reject) {
    request({
      method: 'POST',
      url: 'http://localhost:8080/waterMark/' + ticket,
      callback: function(err, res, doc) {
        if (err == null) {
          resolve(doc);
        } else {
          reject(err);
        }
      }
    });
  });
};

var loop = function loop() {
  if (docs.length > 0) {
    var doc = docs.shift();
    watermark(doc).then(function(ticket) {
      tickets.push(ticket);
      console.log('ticket:', ticket);
    });
    setTimeout(loop, 100 * Math.random());
  }
  if (tickets.length > 0) {
    var ticket = tickets[Math.floor(tickets.length * Math.random())];
    retrieve(ticket).then(function(doc) {
      console.log('doc:', JSON.parse(doc));
    });
  }
}
loop();
