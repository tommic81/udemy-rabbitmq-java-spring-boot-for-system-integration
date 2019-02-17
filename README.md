# Rabbitmq java spring-boot for system integration

#### RabbitMQ

- Provides web interface for management and monitoring
- Built-in user access control
- Built-in REST API
- Multiple programming languages for client

#### Installation

##### Tools Required

- Erlang  [Download Erlang](http://www.erlang.org/downloads).
- RabbitMQ [Download Rabbit](https://www.rabbitmq.com/download.html)
- OpenJDK
- Eclipse IDE

##### Install on Ubuntu

1. Install Ansible
2. Create Ansible script (**udemy-rabbitmq-java-spring-boot-for-system-integration/ansible.txt**)
3. Execute Ansible script (**udemy-rabbitmq-java-spring-boot-for-system-integration/rabbitmq.yml**)

#### RabbitMQ Basic Concepts

- Queue: a buffer that store messages
- Exchange: routing message to queue
- Binding: link between exchange to queue(s)
- Routing key: a key that the exchange looks at to decide how to route the message to queue(s) 

##### Running Docker image 

```shell
docker run -d --hostname rabbit-node --name rabbit -p 5671:5671 -p 5672:5672 -p 15671:15671 -p 15672:15672 rabbitmq:3-management
```

- Go to : [Rabbit console](http://localhost:15672/), guest/guest

#### Starting with coding

- Basic configurations

```properties
# RabbitMQ host
spring.rabbitmq.host=192.15.16.193
 
# RabbitMQ port
spring.rabbitmq.port=5672 
 
# Login credentials
spring.rabbitmq.username=dev
spring.rabbitmq.password=password
```

- Login to consol.
- Create a queue **course.hello**.

```java
//simple producer    
@Autowired
private RabbitTemplate rabbitTemplate;

public void sendHello(String name){
    /** Converts String to byte arrray and sends through default exchange to
    	course.hello queue **/
	rabbitTemplate.convertAndSend("course.hello","Hello " + name);
}

//simple consumer
/** Consuming from course.hello queue**/
@RabbitListener(queues = "course.hello")
public void listen(String message) {
	System.out.println("Consuming: " + message);
}
```

- Create a queue **course.fixedrate**.

```java
//simple producer
/**Sending a message every 500 ms**/
@Scheduled(fixedRate = 500)
public void sendMessage(){
	i++;
	System.out.println("i is " + i);
    rabbitTemplate.convertAndSend("course.fixedrate", "Fixed rate " + i);
}
//simple consumer
@RabbitListener(queues = "course.fixedrate")
public void listen(String message){
	System.out.println("Consuming: " + message);
}
```

##### Multiple Consumers for Each Queue

- Publisher works faster than consumer.
- Consumer bottleneck.
- Solution? Use multiple consumers.

```java
//3 cuncurrent consumers
@RabbitListener(queues = "course.fixedrate", concurrency = "3")
public void listen(String message) throws InterruptedException {
	// simulate long process, up to 2 seconds
	Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
	System.out.println(Thread.currentThread().getName() + " Consuming: " + message);
}
```

#### Working with JSON Message

- Dependencies

```
compile group: 'com.fasterxml.jackson.core', name: 'jackson-core', version: '2.9.6'
compile group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: '2.9.6'
compile group: 'com.fasterxml.jackson.core', name: 'jackson-annotations', version: '2.9.6'
```

- Create a queue **course.employee**.

#### RabbitMQ Exchange

- Exchange distributes (and copy) message to queue(s) based on **Routing key** in message. Routing key is like address in message's envelope.
- **Binding** is link between a message with a queue or some queues.

There are several types of exchange, each routes message differently.

- Fanout
- Direct
- Topic

##### Exchange Type: Fanout

- Multiple queues for single message
- Broadcast to all Queues

###### Coding example

- Create 2 queues: **q.hr.accounting** and **q.hr.marketing**
- Create an exchange: **x.hr**
- Create binding to those two queues

##### Exchange Type: Direct

- Send to selective queues
- Based on routing key
- Message can be discarded

###### Coding example

- Create queues: **q.picture.image** and **q.picture.vector**
- Create an exchange: **x.picture**
- Create bindings:
  - x.picture, routing key = jpg => **q.picture.image**
  - x.picture, routing key = svg => **q.picture.vector**

##### Exchange Type: Topic

- Multiple criteria routing
- Two special characters on routing key
  - *
  - #

###### Coding example

- Create queues: **q.picture.filter**, **q.picture.log**
- Create exchange : **x.picture2** ,type: topic
- Create bindings: 
  - q.picture.image, routing key:  \*.*.png 
  - q.picture.image, routing key: #.jpg
  - q.picture.vector, routing key: \*.*.svg
  - q.picture.filter,  routing key: mobile.#
  - q.picture.log,  routing key: *.large.svg

#### Basic Error Handling

- Message can live forever in a queue, if no consumer that consumes it. Or, message can be discarded and gone if something wrong happened during consumer logic.
-  Messages from a queue can be 'dead-lettered' (sent to another exchange) when:
  - The message is rejected with *requeue=false*
  - The TTL (time to live) for the message expires
  - The queue length limit is exceeded

##### Dead Letter Exchange (DLX)

- Exception might happened
- Spring by default will requeue message
- Infinite consumer loop
- Send problematic message to DLX with requeue = false
- Other consumer can process DLX queue with proper error handling
- Also works for timeout

##### Time To Live (TTL)

- Timeout
- After TTL, message is "dead"
- Queue can be configured to send "dead" message to DLX

##### Consumer Exception without DLX

- Create a new queue: **q.mypicture.image**
- Create an  exchange: **x.mypicture**, type fanout

##### Handle Consumer Exception with DLX (Solution One)

- Create an  exchange: **x.mypicture.dlx**, type fanout
- Create a new queue: **q.mypicture.dlx**
- Bind x.mypicture.dlx => q.mypicture.dlx
- Delete q.mypicture.image and create again but this time specify x.mypicture.dlx as DLX 
- Bind x.mypicture => q.mypicture.image

##### Handle Consumer Exception with DLX (Solution Two)

- Add to **src/main/resources/application.properties**

  ```
  spring.rabbitmq.listener.direct.acknowledge-mode=manual
  spring.rabbitmq.listener.simple.acknowledge-mode=manual
  ```

- Consumer:

```java
  @RabbitListener(queues = "q.mypicture.image")
    public void listen(Message message, Channel channel) throws IOException {

        Picture picture = objectMapper.readValue(message.getBody(), Picture.class);
        if(picture.getSize() > 9000){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
        }else {
            System.out.println("Creating thumbnail & publishing: " + picture);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

```

###### Which approach to choose?

- Automatic rejection is done by throwing `AmqpRejectAndDontRequeueException`.
- Manual rejection by using `Channel` and `channel.basicReject()`. 
- Manual rejection requires to change configuration on Spring's `application.properties`.

- if you're using manual rejection, you must also manually acknowledge processed message using `channel.basicAck()` for all consumers. Otherwise, the message will keep processed everytime consumer restarts.
- **Manual reject is risky. Use Spring to handle  low-level, repetitive tasks.** 

##### TTL Demo

- Create queue: q.mypicture.image2
- Set **Message ttl=5000** and **Dead letter exchange = x.mypicture.dlx** 
- Bind x.mypicture => q.mypicture.image2
- After 5000 ms waiting , the message is sent to DLX

#### Error Handling with Retry Mechanism

- Either keep requeue, or send to DLX
- Need something in between
- Retry for N times
- After more than N, send to DLX

##### Retry Mechanism for Direct Exchange

- Install Postman
- Import colllection from postman-direct-exchange.json
- Verify variables
- Open Runner and execute the collection
- Postmen will create a group of exchanges: x.guideline.* and queues: q.guideline.*

Flow:

```java
/*
GuidelineImageProducer => x.guideline.work 

=> (routing key: jpg, png) q.guideline.image.work
=> (routing key: svg) q.guideline.vector.work

=> GuidelineImageConsumer (listens to  q.guideline.image.work)
=> GuidelineVectorConsumer (listens to  q.guideline.vector.work)
Consumers throw IOExceptions when picture size is too big.

DlxProcessingErrorHandler - handles exceptions
when maxRetryCount is reached, message ist published to the dead exchange: x.guideline.dead
*/
channel.basicPublish(getDeadExchangeName(), 
message.getMessageProperties().getReceivedRoutingKey(),
						null, message.getBody());
channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

// otherwise a message is requeued (rejected)
channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
/*
=> x.guideline.wait
=> (routing key: jpg, png) q.guideline.image.wait
x-dead-letter-exchange:	x.guideline.work
x-message-ttl:	30000
durable:	true

=> (routing key: svg) q.guideline.vector.wait

x-dead-letter-exchange:	x.guideline.work
x-message-ttl:	30000
durable:	true
    
    
after 3000 a message is sent to x.guideline.work */
```

##### Retry Mechanism for Fanout Exchange

- Import colllection from **postman-fanout-exchange.json** to Postman
- Verify variables
- Open Runner and execute the collection
- Postmen will create a group of exchanges: x.guideline2.* and queues: q.guideline2.accounting.\*, q.guideline2.marketing.\* 

Flow:

```java
/* Guideline2EmployeeProducer => x.guideline2.work

=> (routing key: accounting) q.guideline2.accounting.work
=> (routing key: marketing) q.guideline2.marketing.work

=> Guideline2AccountingConsumer (listens to  q.guideline2.accounting.work)
Throws exception when Employee.getName() is empty
Dlx2ProcessingErrorHandler - handles an exception
When maxRetryCount is reached, message ist published to the dead exchange: x.guideline2.dead with a routing key accounting
*/
channel.basicPublish(getDeadExchangeName(), getRoutingKey(), null, message.getBody());
channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

//Otherwise it is rejected.
channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
/*
q.guideline2.accounting.work has settings:
x-dead-letter-exchange:	x.guideline2.wait
x-dead-letter-routing-key:	accounting

Rejected message goes to x.guideline2.wait exchange.
=> q.guideline2.accounting.wait
x-dead-letter-exchange:	x.guideline2.retry
x-dead-letter-routing-key:	accounting
x-message-ttl:	30000

After 30000 ms of waiting on q.guideline2.accounting.wait, message goes to x.guideline2.retry => q.guideline2.accounting.work


=> Guideline2MarketingConsumer (listens to  q.guideline2.marketing.work)
Confirms the message.*/
channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
```

#### Custom  Rest API

- Create producer REST endpoint

  ```java
  @PostMapping(path = {"/api/publish/{exchange}/{routingKey}", "/api/publish/{exchange}"}, consumes = MediaType.APPLICATION_JSON_VALUE)
      public ResponseEntity<String> publish(@PathVariable(name = "exchange", required = true) String exchange,
  @PathVariable(name = "routingKey", required = false) Optional<String> routingKey,
  @RequestBody String message) {
  //............
    }
  ```

- Open Postman

- Sent POST request to **http://localhost:8080/api/publish/x.hr** with JSON:

  ```json
  {
  	"keyOne": "valueOne"
  }
  ```

- The message will be waiting on **q.hr.accounting**

- Send another message to http://localhost:8080/api/publish/x.picture/jpg

  ```json
  {
  	"keyTwo": "valueTwo"
  }
  ```

- The message will be waiting on **q.picture.image**

##### Scheduler for dirty queues

- Annotate Application starter class

  ```java
  @EnableScheduling
  @SpringBootApplication
  public class RabbitmqProducerApplication implements CommandLineRunner {
  //...
  }
  ```

- Create service class for retrieving queues

  ```java
  @Service
  public class RabbitmqProxyService {
     public List<RabbitmqQueue> getAllQueues() {
          String endpoint = "http://localhost:15672/api/queues";
          HttpEntity httpEntity = new HttpEntity<>(createBasicAuthHeaders());
  
          ResponseEntity<List<RabbitmqQueue>> response = restTemplate.exchange(endpoint, HttpMethod.GET, httpEntity,
                  new ParameterizedTypeReference<List<RabbitmqQueue>>() {
                  });
          return response.getBody();
      }
  }
  
  ```

- Create scheduled class 

  ```java
  @Service
  public class RabbitmqScheduler {
  @Scheduled(fixedDelay =  90000)
  public void sweepDirtyQueues(){
  
  try {
       List<RabbitmqQueue> dirtyQueues = rabbitmqProxyService.getAllQueues().stream().filter(p -> p.isDirty()).collect(Collectors.toList());
              
  dirtyQueues.forEach(q -> log.info("Queue {} has {} unprocessed messages", q.getName(), q.getMessages()));
  
  }catch (Exception e){
     log.error("Can't sweep queues: " + e.getMessage());
  	}
    }
  }
  ```

###### Spring @Scheduled

- `@Scheduled(fixedDelay = 5000)` - 5000ms between end of task 1 and start of task 2,
- `@Scheduled(fixedRate = 5000)` - every 5000ms a new task is started. The new task does not wait for the previous one to be ended,
- `@Scheduled(cron="0 * * * * *")` - cron expression [more details](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html). 

