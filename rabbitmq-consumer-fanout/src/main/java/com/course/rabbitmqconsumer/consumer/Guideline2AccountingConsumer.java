package com.course.rabbitmqconsumer.consumer;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.course.rabbitmqconsumer.entity.Employee;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

@Service
public class Guideline2AccountingConsumer {

	private static final String DEAD_EXCHANGE_NAME = "x.guideline2.dead";
	private static final String ROUTING_KEY = "accounting";

	private static final Logger log = LoggerFactory.getLogger(Guideline2AccountingConsumer.class);
	private Dlx2ProcessingErrorHandler dlx2ProcessingErrorHandler;

	private ObjectMapper objectMapper;

	public Guideline2AccountingConsumer() {
		this.objectMapper = new ObjectMapper();
		this.dlx2ProcessingErrorHandler = new Dlx2ProcessingErrorHandler(DEAD_EXCHANGE_NAME, ROUTING_KEY);
	}

	@RabbitListener(queues = "q.guideline2.accounting.work")
	public void listen(Message message, Channel channel)
			throws InterruptedException, JsonParseException, JsonMappingException, IOException {
		try {
			Employee e = objectMapper.readValue(message.getBody(), Employee.class);

			if (StringUtils.isEmpty(e.getName())) {
				throw new IllegalArgumentException("Name is empty");
			} else {
				log.info("On accounting : " + e);
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			}
		} catch (Exception e) {
			log.warn("Error processing message : " + new String(message.getBody()) + " : " + e.getMessage());
			dlx2ProcessingErrorHandler.handleErrorProcessingMessage(message, channel);
		}

	}
}