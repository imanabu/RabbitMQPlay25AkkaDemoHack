package services

import javax.inject._

import akka.actor.{Actor, ActorSystem, Props}
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client._
import com.typesafe.config.ConfigFactory
import play.Logger
import services.RabbitMQConnection.getConnection

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Created by manabu on 2/6/2017.
  */

object Config {
  val RABBITMQ_HOST: String = ConfigFactory.load().getString("rabbitmq.host")
  val RABBITMQ_QUEUE: String = ConfigFactory.load().getString("rabbitmq.queue")
  val RABBITMQ_EXCHANGE: String = ConfigFactory.load().getString("rabbitmq.exchange")
}

object RabbitMQConnection {
  private val connection: Connection = null

  /**
    * Return a connection if one doesn't exist. Else create
    * a new one
    */
  def getConnection: Connection = {
    connection match {
      case null =>
        val factory = new ConnectionFactory()
        factory.setHost(Config.RABBITMQ_HOST)
        factory.newConnection()
      case _ => connection
    }
  }
}

@Singleton
class Sender @Inject() (actorSystem: ActorSystem)(implicit exec: ExecutionContext) {

  Logger.debug("Sender instantiating")

  val connection: Connection = getConnection
  // create the channel we use to send
  val sendingChannel: Channel = connection.createChannel()
  // make sure the queue exists we want to send to
  sendingChannel.queueDeclare(Config.RABBITMQ_QUEUE, false, false, false, null)

  val callback1: (String) => Unit = (x: String) => Logger.info("Received on queue callback 1: " + x)

  setupListener(connection.createChannel(),Config.RABBITMQ_QUEUE, callback1)

  // create an actor that starts listening on the specified queue and passes the
  // received message to the provided callback
  val callback2: (String) => Unit = (x: String) => Logger.info("Received on queue callback 2: " + x)

  // setup the listener that sends to a specific queue using the SendingActor
  setupListener(connection.createChannel(),Config.RABBITMQ_QUEUE, callback2)

  actorSystem.scheduler.schedule(2.seconds, 1.seconds
    , actorSystem.actorOf(Props(
      new SendingActor(channel = sendingChannel,
        queue = Config.RABBITMQ_QUEUE)))
    , "MSG to Queue")

  private def setupListener(receivingChannel: Channel, queue: String, f: (String) => Any) {
    actorSystem.scheduler.scheduleOnce(2.seconds,
      actorSystem.actorOf(Props(new ListeningActor(receivingChannel, queue, f))), "")
  }
}

class SendingActor(channel: Channel, queue: String) extends Actor {

  def receive: PartialFunction[Any, Unit] = {
    case some: String =>
      val msg = some + " : " + System.currentTimeMillis()
      channel.basicPublish("", queue, null, msg.getBytes())
      Logger.info(msg)
    case _ =>
  }
}

class ListeningActor(channel: Channel, queue: String, f: (String) => Any) extends Actor {

  // called on the initial run
  def receive: PartialFunction[Any, Unit] = {
    case _ => startReceiving()
  }

  def startReceiving(): Unit = {

    val consumer = new DefaultConsumer(channel) {

      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]): Unit =
      {
        val msg = new String(body)
        // send the message to the provided callback function
        // and execute this in a sub-actor
        context.actorOf(Props(new Actor {
          def receive: PartialFunction[Any, Unit] = {
            case some: String => f(some)
          }
        })) ! msg
        super.handleDelivery(consumerTag, envelope, properties, body)
      }
    }
    channel.basicConsume(queue, true, consumer)
  }
}

