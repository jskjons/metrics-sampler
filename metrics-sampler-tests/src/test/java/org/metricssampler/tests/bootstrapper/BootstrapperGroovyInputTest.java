package org.metricssampler.tests.bootstrapper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.metricssampler.config.Configuration;
import org.metricssampler.extensions.groovy.GroovyInputConfig;

public class BootstrapperGroovyInputTest extends BootstrapperTestBase {
	@Test
	public void bootstrapComplete() {
		final Configuration config = configure("groovy/complete_script_body.xml");
		
		final GroovyInputConfig item = assertSingleInput(config, GroovyInputConfig.class);
		assertComplete(item);
	}

	private void assertComplete(final GroovyInputConfig item) {
		assertEquals("groovy_complete_script_body", item.getName());
		assertEquals("println 'hello world'", item.getScriptText());
		assertSingleStringVariable(item.getVariables(), "string", "value");
		assertEquals(2, item.getArguments().size());
		assertEquals("arg1", item.getArguments().get(0));
		assertEquals("arg2", item.getArguments().get(1));
	}

	@Test
	public void bootstrapExternalScript() {
		final Configuration config = configure("groovy/external_script.xml");
		
		final GroovyInputConfig item = assertSingleInput(config, GroovyInputConfig.class);
		assertEquals("groovy_external_script", item.getName());
        assertEquals("println 'hello from file'", item.getScriptText());
	}
}
