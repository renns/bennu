package com.qoid.bennu.distributed

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.Config
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.PeerId
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import m3.LockFreeMap

@Singleton
class RabbitMessageQueue @Inject()(config: Config) extends MessageQueue {
  private val factory = new ConnectionFactory()
  factory.setUri(config.amqpUri)

  private val rabbitConnection = factory.newConnection()

  private val consumers = new LockFreeMap[(PeerId, PeerId), DefaultConsumer]

  private val persistentJson: AMQP.BasicProperties = new AMQP.BasicProperties("application/json", null, null, 2, 0, null, null, null, null, null, null, null, null, null)

  override def subscribe(connections: List[Connection], fn: InternalId => DistributedMessage => Unit): Unit = {
    connections.foreach {
      connection =>
        val (queue, channel) = createChannel(connection.remotePeerId, connection.localPeerId)
        val consumer = new RabbitMessageQueue.Consumer(channel, fn(connection.iid))
        channel.basicConsume(queue, true, consumer)
        consumers.put((connection.remotePeerId, connection.localPeerId), consumer)
    }
  }

  override def unsubscribe(connection: Connection): Unit = {
    consumers.remove((connection.remotePeerId, connection.localPeerId)).foreach {
      consumer =>
        val channel = consumer.getChannel
        channel.basicCancel(consumer.getConsumerTag)
        channel.close()
    }
  }

  override def enqueue(connection: Connection, message: DistributedMessage): Unit = {
    val (queue, channel) = createChannel(connection.localPeerId, connection.remotePeerId)
    channel.basicPublish("", queue, persistentJson, message.toJson.toJsonStr.getBytes("UTF-8"))
    channel.close()
  }

  private def createChannel(fromPeerId: PeerId, toPeerId: PeerId): (String, Channel) = {
    val queue = s"${fromPeerId.value}-${toPeerId.value}"
    val channel = rabbitConnection.createChannel()
    channel.queueDeclare(queue, true, false, false, null)
    (queue, channel)
  }
}

object RabbitMessageQueue {
  class Consumer(channel: Channel, fn: DistributedMessage => Unit) extends DefaultConsumer(channel) {
    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
      fn(DistributedMessage.fromJson(parseJson(new String(body, "UTF-8"))))
    }
  }
}
