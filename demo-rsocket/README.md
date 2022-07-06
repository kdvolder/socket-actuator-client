Rsocktuator-Controller
======================

This is a second attempt at answering the call to 'expose actuators over (R)Socket'. 

In this experiment / exploration attempt instead of using plain Sockets directly,
instead the goal here is to:

- Use RSocket
- Use Spring Messaging layer on top of RSocket to handle:
   - communication patterns such as 'Request <-> Response'
   - dispatching request to specific operations
   - handling encoding and decoding of requests, responses and 'payload' data.
   
Some questions we are seeking answers to:

- how hard is this? 
- can we make satisfy the 'independently configured' requirement 
  (i.e. an 'infrastructure' component should be able to configure its own
   'dedicated' port/rsocket server without interferring with or breaking
   a user's pre-existing RSocket / Spring Messaging / Object-encoding config.
- how well can something like this co-exist with a user's own code, 
  particularly if the user's app is not 'reactive' (e.g an 'old-fashioned
  embedded Tomcat setup). 