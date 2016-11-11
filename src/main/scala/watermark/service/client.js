'use strict';

var request = require('request');

// node Client.js

var docs = [
  'book\tCosmos\tCarl Sagan\tScience',
  'journal\tThe Journal of cell biology\tRockefeller University Press',
  'book\tA brief history of time\tStephen W Hawking\tScience',
  'book\tPale Blue Dot\tCarl Sagan\tScience',
  'journal\tNational Geographic\tRockefeller University Press',
  'book\tThe grand Design\tStephen W Hawking\tScience',
  'book\tContact\tCarl Sagan\tScience',
  'journal\tScientific American\tRockefeller University Press',
  'book\tThe Universe in a Nutshell\tStephen W Hawking\tScience',
  'book\tThe Dragons of Eden\tCarl Sagan\tScience',
  'journal\tNature\tRockefeller University Press',
  'book\tBlack Holes and Baby Universes\tStephen W Hawking\tScience',
];

var tickets = [];

//  watermark :: Document -> Promise Ticket
var watermark = function(doc) {
  return new Promise(function(resolve, reject) {
    request({
      method: 'POST',
      url: 'http://localhost:8080/ticket',
      body: doc,
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
  //Mimic 12 clients requesting documents be watermarked at once.
  if (docs.length > 0) {
    var doc = docs.shift();
    //Rest API for retieving a ticket for a Document being watermarked in the future.
    watermark(doc).then(function(ticket) {
      tickets.push(ticket);
      console.log('ticket created:', ticket);
      console.log('tickets are: ' + tickets);
    });
    setTimeout(loop, 100 * Math.random());
  }
  //If a user has retrieved a ticket, then model the user requesting the
  //waterMark by ticket.
  if (tickets.length > 0) {
    var ticket = tickets[Math.floor(tickets.length * Math.random())];
    //Rest API for retrieving a Document.
    retrieve(ticket).then(function(doc) {
      console.log('doc:' + doc);
      //console.log('doc:', JSON.parse(doc));
    });
  }
}
loop();
