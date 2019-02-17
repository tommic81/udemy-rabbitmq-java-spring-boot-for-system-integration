package com.course.rabbitmqconsumer.consumer;

import com.course.rabbitmqconsumer.entity.Picture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MyPictureImageConsumer {

    private ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "q.mypicture.image")
    public void listen(Message message, Channel channel) throws IOException {

        Picture picture = objectMapper.readValue(message.getBody(), Picture.class);
        if(picture.getSize() > 9000){
           // throw  new AmqpRejectAndDontRequeueException("Picture size too large: "  + picture);

            channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
        }else {
            System.out.println("Creating thumbnial & publishing: " + picture);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        }
    }
}
