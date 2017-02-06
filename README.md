# Play 2.5 Seed with RabbitMQ Akka Integration Demo Hack

Manabu Tokunaga
[WinguMD, Inc.](https://wingumd.com)
6 Feb 2017

## About This Demo Hack

This hack is based on the sample code provided in
[DZone Integration Zone article by Jos Dirksen](https://dzone.com/articles/connect-rabbitmq-using-scala).

It has been implemented on a bare-bone Activator Play 2.5.10 seed (activator new), as such all of the
original seed function have not been touched.

You will expect on the console that RabbitMQ is being updated as soon as the web page shows on the browser.

## Controllers (Not Touched)

- HomeController.scala:

  Shows how to handle simple HTTP requests.

- AsyncController.scala:

  Shows how to do asynchronous programming when handling a request.

- CountController.scala:

  Shows how to inject a component into a controller and use the component when
  handling requests.


## Components (New Code added and Modified)

- rmqServices.scala (New)

  This code has been updated to work in Play Framework 2.5 and also with Guice dependency
  injection to get the RabbitMQ talker and listner going.

- Module.scala (Modified):

  Shows how to use Guice to bind all the components needed by your application.
  Altered in this hack to launch RabbitMQ Sender function.

- Counter.scala:

  An example of a component that contains state, in this case a simple counter.

- ApplicationTimer.scala:

  An example of a component that starts when the application starts and stops
  when the application stops.

## Filters (Not Touched)

- Filters.scala:

  Creates the list of HTTP filters used by your application.

- ExampleFilter.scala

  A simple filter that adds a header to every response.
  