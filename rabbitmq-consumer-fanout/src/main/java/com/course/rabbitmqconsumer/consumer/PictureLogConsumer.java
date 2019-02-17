package com.course.rabbitmqconsumer.consumer;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.course.rabbitmqconsumer.entity.Picture;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PictureLogConsumer {

	private ObjectMapper objectMapper = new ObjectMapper();

	@RabbitListener(queues = "q.picture.log")
	public void listen(String message)
			throws InterruptedException, JsonParseException, JsonMappingException, IOException {
		Picture p = objectMapper.readValue(message, Picture.class);
		// process the image
		System.out.println("Logging large vector : " + p);
	}

}
