package com.course.rabbitmqproducer;

import com.course.rabbitmqproducer.entity.Employee;
import com.course.rabbitmqproducer.entity.Picture;
import com.course.rabbitmqproducer.producer.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@EnableScheduling
@SpringBootApplication
public class RabbitmqProducerApplication implements CommandLineRunner {

	@Autowired
	private MyPictureProducer myPictureProducer;
	private  final List<String> TYPES = Arrays.asList("jpg", "png", "svg");
	private  final List<String> SOURCES = Arrays.asList("mobile", "web");

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqProducerApplication.class, args);
	}

	@Override
	public void run(String... args) throws JsonProcessingException {
		for(int i=0; i< 1; i++){

			Picture p = new Picture();
			p.setName("Picture" + i);
			p.setSize(ThreadLocalRandom.current().nextLong(9001, 10000));
			p.setSource(SOURCES.get(i % SOURCES.size()));
			p.setType(TYPES.get(i % TYPES.size()));

			myPictureProducer.sendMessage(p);
		}

	}
}

