package com.course.rabbitmqconsumer.consumer;

import com.course.rabbitmqconsumer.entity.Picture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PictureFilterConsumer {

    private ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "q.picture.filter")
    public void listen(String message) throws IOException {

        Picture picture = objectMapper.readValue(message, Picture.class);
        System.out.println("Applying filter: " + picture);

    }
}
