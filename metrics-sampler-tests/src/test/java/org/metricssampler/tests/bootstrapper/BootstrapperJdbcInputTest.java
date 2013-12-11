package org.metricssampler.tests.bootstrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.metricssampler.config.Configuration;
import org.metricssampler.extensions.jdbc.JdbcConnectionPoolConfig;
import org.metricssampler.extensions.jdbc.JdbcInputConfig;
import org.metricssampler.extensions.jdbc.JdbcSharedQueriesConfig;

public class BootstrapperJdbcInputTest extends BootstrapperTestBase {
	@Test
	public void bootstrapComplete() {
		final Configuration config = configure("jdbc/complete.xml");
		
		final JdbcInputConfig item = assertInput(config, "jdbc", JdbcInputConfig.class);
		assertComplete(config, item);
		
		assertInput(config, "jdbc2", JdbcInputConfig.class);
	}

	private void assertComplete(final Configuration config, final JdbcInputConfig item) {
		assertEquals("jdbc", item.getName());
		assertEquals("pool", item.getPool());
		assertEquals(2, item.getQueries().size());
		assertTrue(item.getQueries().contains("select 'first' from dual"));
		assertTrue(item.getQueries().contains("select 'second' from dual"));
		assertSingleStringVariable(item.getVariables(), "string", "value");
		
		final JdbcConnectionPoolConfig pool = assertSharedResource(config, "pool", JdbcConnectionPoolConfig.class);
		assertEquals("pool", pool.getName());
		assertEquals("username", pool.getUsername());
		assertEquals("password", pool.getPassword());
		assertEquals("url", pool.getUrl());
		assertEquals("driver", pool.getDriver());
		assertEquals(10, pool.getMinSize());
		assertEquals(20, pool.getMaxSize());
		assertEquals(3, pool.getLoginTimeout());
		
		JdbcSharedQueriesConfig queries = assertSharedResource(config, "shared-queries", JdbcSharedQueriesConfig.class);
		assertEquals(2, queries.getQueries().size());
	}

	@Test
	public void bootstrapTemplate() {
		final Configuration config = configure("jdbc/template.xml");
		
		final JdbcInputConfig item = assertInput(config, "jdbc", JdbcInputConfig.class);
		assertComplete(config, item);
	}

	@Test
	public void bootstrapMinimal() {
		final Configuration config = configure("jdbc/minimal.xml");

		final JdbcInputConfig item = assertSingleInput(config, JdbcInputConfig.class);
		assertEquals("jdbc", item.getName());
		assertEquals("pool", item.getPool());
		assertEquals(1, item.getQueries().size());
		assertTrue(item.getQueries().contains("select 'first' from dual"));
		assertTrue(item.getVariables().isEmpty());
		
		final JdbcConnectionPoolConfig pool = assertSingleSharedResource(config, JdbcConnectionPoolConfig.class);
		assertEquals("pool", pool.getName());
		assertEquals("username", pool.getUsername());
		assertEquals("password", pool.getPassword());
		assertEquals("url", pool.getUrl());
		assertEquals("driver", pool.getDriver());
		assertEquals(10, pool.getMinSize());
		assertEquals(20, pool.getMaxSize());
		assertEquals(5, pool.getLoginTimeout());
	}
}
