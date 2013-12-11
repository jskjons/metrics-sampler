package org.metricssampler.extensions.jdbc;

import java.sql.DriverManager;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.metricssampler.config.ConfigurationException;
import org.metricssampler.config.InputConfig;
import org.metricssampler.config.SharedResourceConfig;
import org.metricssampler.reader.MetricsReader;
import org.metricssampler.resources.SharedObjectRegistry;
import org.metricssampler.resources.SharedResource;
import org.metricssampler.service.AbstractExtension;
import org.metricssampler.util.StringUtils;

public class JdbcExtension extends AbstractExtension {
	@Override
	public Collection<Class<?>> getXBeans() {
		final List<Class<?>> result = new LinkedList<Class<?>>();
		result.add(JdbcInputXBean.class);
		result.add(JdbcConnectionPoolXBean.class);
		result.add(JdbcSharedQueriesXBean.class);
		return result;
	}

	@Override
	public void initialize() {
		/**
		 * WTF: load the drivers in the caller thread
		 */
		DriverManager.getDrivers();
	}

	@Override
	public boolean supportsInput(final InputConfig config) {
		return config instanceof JdbcInputConfig;
	}

	@Override
	protected MetricsReader doNewReader(final InputConfig config) {
		final JdbcInputConfig jdbcConfig = (JdbcInputConfig) config;
		final SharedResource sharedResource = getGlobalFactory().getSharedResource(jdbcConfig.getPool());
		if (sharedResource instanceof JdbcConnectionPool) {
			return new JdbcMetricsReader(jdbcConfig, (JdbcConnectionPool) sharedResource, getSharedQueries(jdbcConfig));
		} else {
			throw new ConfigurationException(jdbcConfig.getPool() + " is not a JDBC connection pool: " + sharedResource);
		}
	}

	@SuppressWarnings("unchecked")
    private List<String> getSharedQueries(JdbcInputConfig config) {
	    if (!StringUtils.isEmptyOrNull(config.getSharedQueries())) {
	        final SharedResource sharedResource = getGlobalFactory().getSharedResource(config.getSharedQueries());
	        if (sharedResource instanceof SharedObjectRegistry) {
	            return ((SharedObjectRegistry) sharedResource).getObjects();
	        }
	    }
	    return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean supportsSharedResource(final SharedResourceConfig config) {
		return config instanceof JdbcConnectionPoolConfig || config instanceof JdbcSharedQueriesConfig;
	}

	@Override
	protected SharedResource doNewSharedResource(final SharedResourceConfig config) {
	    if (config instanceof JdbcConnectionPoolConfig) {
	        final JdbcConnectionPoolConfig poolConfig = (JdbcConnectionPoolConfig) config;
	        final JdbcConnectionPool result = new JdbcConnectionPool(poolConfig);
	        return result;
	    } else {
	        return new SharedObjectRegistry(config.getName(), ((JdbcSharedQueriesConfig) config).getQueries());
	    }
	}
}
