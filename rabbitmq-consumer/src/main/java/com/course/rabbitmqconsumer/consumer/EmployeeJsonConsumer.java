package com.course.rabbitmqconsumer.consumer;

import com.course.rabbitmqconsumer.entity.Employee;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmployeeJsonConsumer {

    private ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = "course.employee")
    public void listen(String message) throws IOException {
        Employee employee = objectMapper.readValue(message, Employee.class);
        System.out.println(employee);

    }
}
