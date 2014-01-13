package org.metricssampler.extensions.groovy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.metricssampler.config.InputConfig;
import org.metricssampler.config.loader.xbeans.ArgumentXBean;
import org.metricssampler.reader.MetricsReader;
import org.metricssampler.service.AbstractExtension;

public class GroovyExtension extends AbstractExtension {
	@Override
	public Collection<Class<?>> getXBeans() {
		final List<Class<?>> result = new LinkedList<Class<?>>();
		result.add(GroovyInputXBean.class);
		result.add(ArgumentXBean.class);
		return result;
	}
	
	@Override
	public boolean supportsInput(final InputConfig config) {
		return config instanceof GroovyInputConfig;
	}

	@Override
	protected MetricsReader doNewReader(final InputConfig config) {
		return new GroovyMetricsReader((GroovyInputConfig) config);
	}
}
