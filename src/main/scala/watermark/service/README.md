To start the WaterMark Service:

1. From root run "sbt run" to start the Server.
2. From watermark/src/main/scala/watermark/service run "Node client.js". This will start the script that mimics twelve clients sending requests for watermarking a string. Followed by twelve requests to retrieve documents with the document's Ticket.
I don't write JavaScript often so please don't pay too much attention to this part ;)
3. To Run the tests "sbt test".

Two disclaimers:
1. I wrote this in a fairly functional style because that is what I have been doing. I'm open to other styles!
I thought you might enjoy seeing how this would be done with a type safe server.
2. This is my first attempt at using Akka. I look forward to learning more about it! It has been fun!

okay three... This is not as good as code that would be in production, partially because it has had no peer review!
