package com.course.rabbitmqproducer.producer;

import com.course.rabbitmqproducer.entity.Picture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PictureProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void sendMessage(Picture p) throws JsonProcessingException {

        StringBuilder sb = new StringBuilder();
        sb.append(p.getSource());
        sb.append('.');

        if(p.getSize() > 4000){
            sb.append("large");
        }else {
            sb.append("small");
        }

        sb.append('.');
        sb.append(p.getType());

        String routingKey = sb.toString();

        String json = objectMapper.writeValueAsString(p);
        rabbitTemplate.convertAndSend("x.picture2",routingKey, json);
    }
}
