package com.course.rabbitmqproducer.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class RabbitmqRestController {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqRestController.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean isValidJson(String string) {
        try {
            objectMapper.readTree(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;



    @PostMapping(path = {"/api/publish/{exchange}/{routingKey}", "/api/publish/{exchange}"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> publish(@PathVariable(name = "exchange", required = true) String exchange,
                                          @PathVariable(name = "routingKey", required = false) Optional<String> routingKey,
                                          @RequestBody String message) {
        if (!isValidJson(message)) {
            return new ResponseEntity<>(Boolean.FALSE.toString(), HttpStatus.BAD_REQUEST);
        }

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey.orElse(""), message);
            return new ResponseEntity<>(Boolean.TRUE.toString(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
