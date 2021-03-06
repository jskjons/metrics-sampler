package org.metric.sampler.extension.redis;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("redis-llen")
public class RedisLLenCommandXBean extends RedisCommandXBean {
	@XStreamAsAttribute
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	@Override
	public RedisCommand toConfig() {
		return new RedisLLenCommand(getDatabaseInt(), key);
	}
}
