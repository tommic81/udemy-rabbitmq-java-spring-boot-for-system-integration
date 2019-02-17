package com.course.rabbitmqproducer.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class FixedRateProducer {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	private int i = 0;

	@Scheduled(fixedRate = 500)
	public void sendMessage() {
		i++;
//		System.out.println("i is " + i);
		rabbitTemplate.convertAndSend("course.fixedrate", "Fixed rate " + i);
	}

}
