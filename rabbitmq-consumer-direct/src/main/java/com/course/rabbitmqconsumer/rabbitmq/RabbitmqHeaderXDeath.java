package com.course.rabbitmqconsumer.rabbitmq;

import java.util.Date;
import java.util.List;

/**
 * Represents RabbitMQ Header, part x-death. Tested on RabbitMQ 3.7.x.
 * 
 * @author timpamungkas
 */
public class RabbitmqHeaderXDeath {

	private int count;
	private String exchange;
	private String queue;
	private String reason;
	private List<String> routingKeys;
	private Date time;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RabbitmqHeaderXDeath other = (RabbitmqHeaderXDeath) obj;
		if (count != other.count) {
			return false;
		}
		if (exchange == null) {
			if (other.exchange != null) {
				return false;
			}
		} else if (!exchange.equals(other.exchange)) {
			return false;
		}
		if (queue == null) {
			if (other.queue != null) {
				return false;
			}
		} else if (!queue.equals(other.queue)) {
			return false;
		}
		if (reason == null) {
			if (other.reason != null) {
				return false;
			}
		} else if (!reason.equals(other.reason)) {
			return false;
		}
		if (routingKeys == null) {
			if (other.routingKeys != null) {
				return false;
			}
		} else if (!routingKeys.equals(other.routingKeys)) {
			return false;
		}
		if (time == null) {
			if (other.time != null) {
				return false;
			}
		} else if (!time.equals(other.time)) {
			return false;
		}
		return true;
	}

	public int getCount() {
		return count;
	}

	public String getExchange() {
		return exchange;
	}

	public String getQueue() {
		return queue;
	}

	public String getReason() {
		return reason;
	}

	public List<String> getRoutingKeys() {
		return routingKeys;
	}

	public Date getTime() {
		return time;
	}

	@Override
	public int hashCode() {
		final int prime = 19;
		int result = 1;
		result = prime * result + count;
		result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + ((queue == null) ? 0 : queue.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		result = prime * result + ((routingKeys == null) ? 0 : routingKeys.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		return result;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setRoutingKeys(List<String> routingKeys) {
		this.routingKeys = routingKeys;
	}

	public void setTime(Date time) {
		this.time = time;
	}

}
