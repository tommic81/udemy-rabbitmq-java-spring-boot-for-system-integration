package com.course.rabbitmqconsumer.consumer;

import java.io.IOException;

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
public class Guideline2MarketingConsumer {

	private static final Logger log = LoggerFactory.getLogger(Guideline2MarketingConsumer.class);
	private ObjectMapper objectMapper;

	public Guideline2MarketingConsumer() {
		this.objectMapper = new ObjectMapper();
	}

	@RabbitListener(queues = "q.guideline2.marketing.work")
	public void listen(Message message, Channel channel)
			throws InterruptedException, JsonParseException, JsonMappingException, IOException {
		Employee e = objectMapper.readValue(message.getBody(), Employee.class);
		log.info("On marketing : " + e);
		channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
	}
}