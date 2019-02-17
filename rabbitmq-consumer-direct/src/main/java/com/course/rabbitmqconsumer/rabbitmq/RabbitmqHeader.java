package com.course.rabbitmqconsumer.rabbitmq;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents RabbitMQ Header. Tested on RabbitMQ 3.7.x.
 * 
 * @author timpamungkas
 */
public class RabbitmqHeader {

	private static final String KEYWORD_QUEUE_WAIT = "wait";
	private static final String EMPTY_STRING = "";
	private List<RabbitmqHeaderXDeath> xDeaths = new ArrayList<>(2);
	private String xFirstDeathExchange = EMPTY_STRING;
	private String xFirstDeathQueue = EMPTY_STRING;
	private String xFirstDeathReason = EMPTY_STRING;

	@SuppressWarnings("unchecked")
	public RabbitmqHeader(Map<String, Object> headers) {
		if (headers != null) {
			Optional xFirstDeathExchange = Optional.ofNullable(headers.get("x-first-death-exchange"));
			Optional xFirstDeathQueue = Optional.ofNullable(headers.get("x-first-death-queue"));
			Optional xFirstDeathReason = Optional.ofNullable(headers.get("x-first-death-reason"));

			xFirstDeathExchange.ifPresent(s -> this.setxFirstDeathExchange(s.toString()));
			xFirstDeathQueue.ifPresent(s -> this.setxFirstDeathQueue(s.toString()));
			xFirstDeathReason.ifPresent(s -> this.setxFirstDeathReason(s.toString()));

			List<Map<String, Object>> xDeathHeaders = (List<Map<String, Object>>) headers.get("x-death");

			if (xDeathHeaders != null) {
				for (Map<String, Object> x : xDeathHeaders) {
					RabbitmqHeaderXDeath hdrDeath = new RabbitmqHeaderXDeath();
					Optional reason = Optional.ofNullable(x.get("reason"));
					Optional count = Optional.ofNullable(x.get("count"));
					Optional exchange = Optional.ofNullable(x.get("exchange"));
					Optional queue = Optional.ofNullable(x.get("queue"));
					Optional routingKeys = Optional.ofNullable(x.get("routing-keys"));
					Optional time = Optional.ofNullable(x.get("time"));

					reason.ifPresent(s -> hdrDeath.setReason(s.toString()));
					count.ifPresent(s -> hdrDeath.setCount(Integer.parseInt(s.toString())));
					exchange.ifPresent(s -> hdrDeath.setExchange(s.toString()));
					queue.ifPresent(s -> hdrDeath.setQueue(s.toString()));
					routingKeys.ifPresent(r -> {
						List<String> listR = (List<String>) r;
						hdrDeath.setRoutingKeys(listR);
					});
					time.ifPresent(d -> hdrDeath.setTime((Date) d));

					xDeaths.add(hdrDeath);
				}
			}
		}
	}

	public int getFailedRetryCount() {
		// get from queue "wait"
		for (RabbitmqHeaderXDeath xDeath : xDeaths) {
			if (xDeath.getExchange().toLowerCase().endsWith(KEYWORD_QUEUE_WAIT)
					&& xDeath.getQueue().toLowerCase().endsWith(KEYWORD_QUEUE_WAIT)) {
				return xDeath.getCount();
			}
		}

		return 0;
	}

	public List<RabbitmqHeaderXDeath> getxDeaths() {
		return xDeaths;
	}

	public String getxFirstDeathExchange() {
		return xFirstDeathExchange;
	}

	public String getxFirstDeathQueue() {
		return xFirstDeathQueue;
	}

	public String getxFirstDeathReason() {
		return xFirstDeathReason;
	}

	public void setxDeaths(List<RabbitmqHeaderXDeath> xDeaths) {
		this.xDeaths = xDeaths;
	}

	public void setxFirstDeathExchange(String xFirstDeathExchange) {
		this.xFirstDeathExchange = xFirstDeathExchange;
	}

	public void setxFirstDeathQueue(String xFirstDeathQueue) {
		this.xFirstDeathQueue = xFirstDeathQueue;
	}

	public void setxFirstDeathReason(String xFirstDeathReason) {
		this.xFirstDeathReason = xFirstDeathReason;
	}

}
