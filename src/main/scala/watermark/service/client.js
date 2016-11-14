'use strict';

var request = require('request');

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

//Store tickets to represent Clients that have recieved a Ticket
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
  //Q: js promises?
  return new Promise(function(resolve, reject) {
    request({
      method: 'POST',
      url: 'http://localhost:8080/waterMark/' + ticket,
      callback: function(err, res, doc) {
        console.log(res.statusCode)
        if (err == null) {
          resolve(doc);
        } else {
          reject(err);
        }
      }
    });
  });
};

//Q: js event loop?
function simulateWaterMarkRequest() {
  //Mimic 12 clients requesting documents to be watermarked at once.
  if (docs.length > 0) {
    var doc = docs.shift();
    //Rest API for retieving a ticket for a Document being watermarked in the future.
    watermark(doc).then(function(ticket) {
      tickets.push(ticket);
      console.log('ticket created:', ticket);
      console.log('tickets are: ' + tickets);
    });
    //Simulate request traffic
    setTimeout(simulateWaterMarkRequest, 100 * Math.random());
  }
}

//If a user has retrieved a ticket, then model the user requesting the waterMark by ticket.
function simulateRetrieveDocument() {
  if (tickets.length > 0) {
    var ticket = tickets[Math.floor(tickets.length * Math.random())];
    //Rest API for retrieving a Document.
    //"Then" is equivalent to Future.onComplete, a nicer way of handling side
    // effects than using callbacks.
    retrieve(ticket).then(function(doc) {
      console.log('doc:' + doc);
      console.log("");
      //console.log('doc:', JSON.parse(doc));
    });
  }
  setTimeout(simulateRetrieveDocument, 100 * Math.random());
}
simulateWaterMarkRequest();
//simulateRetrieveDocument();
