package com.course.rabbitmqproducer;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.course.rabbitmqproducer.entity.Employee;
import com.course.rabbitmqproducer.producer.Guideline2EmployeeProducer;

@SpringBootApplication
public class RabbitmqProducerApplication implements CommandLineRunner {

	@Autowired
	private Guideline2EmployeeProducer guideline2EmployeeProducer;

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqProducerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// For sample purpose. Will throw invalid message (name is null)
		for (int i = 0; i < 10; i++) {
			Employee e = new Employee("emp" + i, null, new Date());
			guideline2EmployeeProducer.sendMessage(e);
		}
	}

}
