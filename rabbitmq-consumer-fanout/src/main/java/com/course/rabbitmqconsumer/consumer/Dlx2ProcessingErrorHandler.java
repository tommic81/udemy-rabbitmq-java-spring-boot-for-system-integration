package com.course.rabbitmqconsumer.consumer;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.lang.NonNull;

import com.course.rabbitmqconsumer.rabbitmq.RabbitmqHeader;
import com.rabbitmq.client.Channel;

/**
 * 
 * <p>
 * Generic class to handle RabbitMQ proccessing error that might occur on
 * <code>try-catch</code>. This will not handle invalid message conversion
 * though (for example if you has Employee JSON structure to process, but got
 * Animal JSON structure instead from Rabbit MQ queue).
 * </p>
 * 
 * <p>
 * In short, this is just a class to avoid boilerplate codes for your handler.
 * Default implementation is re-throw message to dead letter exchange, using
 * <code>DlxProcessingErrorHandler</code> class. The basic usage of the
 * interface is :<br/>
 * 
 * <pre>
 * public void handleMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
 * 	var jsonObjectToBeProcessed = null;
 * 
 * 	try {
 * 		jsonObjectToBeProcessed = objectMapper.readValue(new String(message.getBody()),
 * 				JsonObjectToBeProcessed.class);
 * 
 * 		// do real processing here
 * 		// ...
 * 		//
 * 
 * 		channel.basicAck(tag, false);
 * 	} catch (Exception e) {
 * 		processingErrorHandler.handleErrorProcessingMessage(message, channel, tag);
 * 	}
 * }
 * </pre>
 * 
 * @author timpamungkas
 *
 */
public class Dlx2ProcessingErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(Dlx2ProcessingErrorHandler.class);

	/**
	 * Dead exchange name
	 */
	@NonNull
	private String deadExchangeName;

	@NonNull
	private String routingKey;

	private int maxRetryCount = 3;

	/**
	 * Constructor. Will retry for n times (default is 3) and on the next retry will
	 * consider message as dead, put it on dead exchange with given
	 * <code>dlxExchangeName</code> and <code>routingKey</code>
	 * 
	 * @param deadExchangeName dead exchange name. Not a dlx for work queue, but
	 *                         exchange name for really dead message (wont processed
	 *                         antmore).
	 * @param routingKey       dead letter routing key
	 * @throws IllegalArgumentException if <code>dlxExchangeName</code> or
	 *                                  <code>dlxRoutingKey</code> is null or empty.
	 */
	public Dlx2ProcessingErrorHandler(String deadExchangeName, String routingKey) throws IllegalArgumentException {
		super();

		if (StringUtils.isAnyEmpty(deadExchangeName, routingKey)) {
			throw new IllegalArgumentException("Must define dlx exchange name and routing key");
		}

		this.deadExchangeName = deadExchangeName;
		this.routingKey = routingKey;
	}

	/**
	 * Constructor. Will retry for <code>maxRetryCount</code> times and on the next
	 * retry will consider message as dead, put it on dead exchange with given
	 * <code>dlxExchangeName</code> and <code>routingKey</code>
	 * 
	 * @param deadExchangeName dead exchange name. Not a dlx for work queue, but
	 *                         exchange name for really dead message (wont processed
	 *                         antmore).
	 * @param maxRetryCount    number of retry before message considered as dead (0
	 *                         >= <code> maxRetryCount</code> >= 1000). If set less
	 *                         than 0, will always retry
	 * @throws IllegalArgumentException if <code>dlxExchangeName</code> or
	 *                                  <code>dlxRoutingKey</code> is null or empty.
	 */

	public Dlx2ProcessingErrorHandler(String deadExchangeName, String routingKey, int maxRetryCount) {
		this(deadExchangeName, routingKey);
		setMaxRetryCount(maxRetryCount);
	}

	public String getDeadExchangeName() {
		return deadExchangeName;
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	/**
	 * Handle AMQP message consume error. This default implementation will put
	 * message to dead letter exchange for <code>maxRetryCount</code> times, thus
	 * two variables are required when creating this object:
	 * <code>dlxExchangeName</code> and <code>dlxRoutingKey</code>. <br/>
	 * <code>maxRetryCount</code> is 3 by default, but you can set it using
	 * <code>setMaxRetryCount(int)</code>
	 * 
	 * @param message AMQP message that caused error
	 * @param channel channel for AMQP message
	 * @param tag     message delivery tag
	 * @return <code>true</code> if error handler works sucessfully,
	 *         <code>false</code> otherwise
	 */
	public boolean handleErrorProcessingMessage(Message message, Channel channel) {
		RabbitmqHeader rabbitMqHeader = new RabbitmqHeader(message.getMessageProperties().getHeaders());

		try {
			if (rabbitMqHeader.getFailedRetryCount() >= maxRetryCount) {
				// publish to dead and ack
				log.warn("[DEAD] Error at " + new Date() + " on retry " + rabbitMqHeader.getFailedRetryCount()
						+ " for message " + message);

				channel.basicPublish(getDeadExchangeName(), getRoutingKey(), null, message.getBody());
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				log.debug("[REQUEUE] Error at " + new Date() + " on retry " + rabbitMqHeader.getFailedRetryCount()
						+ " for message " + message);

				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			}
			return true;
		} catch (IOException e) {
			log.warn("[HANDLER-FAILED] Error at " + new Date() + " on retry " + rabbitMqHeader.getFailedRetryCount()
					+ " for message " + message);
		}

		return false;
	}

	public void setMaxRetryCount(int maxRetryCount) throws IllegalArgumentException {
		if (maxRetryCount > 1000) {
			throw new IllegalArgumentException("max retry must between 0-1000");
		}

		this.maxRetryCount = maxRetryCount;
	}

}
