package org.metricssampler.extensions.jdbc;

import static org.metricssampler.util.Preconditions.checkArgumentNotNull;
import static org.metricssampler.util.Preconditions.checkArgumentNotNullNorEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.metricssampler.config.InputConfig;
import org.metricssampler.util.StringUtils;

public class JdbcInputConfig extends InputConfig {
	private final String pool;
	private final String sharedQueries;
	private final List<String> queries;
	
	public JdbcInputConfig(final String name, final Map<String, Object> variables, final String pool, final List<String> queries, String sharedQueries) {
		super(name, variables);
		checkArgumentNotNull(pool, "pool");
		if (StringUtils.isEmptyOrNull(sharedQueries)) {
		    checkArgumentNotNullNorEmpty(queries, "queries");
		}
		this.sharedQueries = sharedQueries;
		this.pool = pool;
		this.queries = Collections.unmodifiableList(queries != null ? queries : new ArrayList<String>());
	}

	public String getPool() {
		return pool;
	}

	public List<String> getQueries() {
		return queries;
	}

    public String getSharedQueries() {
        return sharedQueries;
    }
}
