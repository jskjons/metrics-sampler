package org.metricssampler.extensions.groovy;

import groovy.lang.GroovyShell;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.metricssampler.reader.AbstractMetricsReader;
import org.metricssampler.reader.BulkMetricsReader;
import org.metricssampler.reader.MetricName;
import org.metricssampler.reader.MetricValue;
import org.metricssampler.reader.SimpleMetricName;

/**
 * Execute a groovy script on each sample and read metrics from script output in form Map[name -> value] or List[List[name, value, Date]]
 */
public class GroovyMetricsReader extends AbstractMetricsReader<GroovyInputConfig> implements BulkMetricsReader {

	private String scriptText;
	
    public GroovyMetricsReader(final GroovyInputConfig config) {
		super(config);
		scriptText = config.getScriptText();
	}

	@Override
	public void open() {}

	@Override
	public void close() {}

	@Override
	public Iterable<MetricName> readNames() {
		return readAllMetrics().keySet();
	}

    @Override
    @SuppressWarnings("unchecked")
	public Map<MetricName, MetricValue> readAllMetrics() {
	    final Map<MetricName, MetricValue> result = new HashMap<MetricName, MetricValue>();
	    
	    GroovyShell shell = new GroovyShell();
	    shell.setVariable("args", config.getArguments());
        Object scriptResult = shell.evaluate(scriptText);
	    if (scriptResult instanceof Map) {
	        for (Entry<?,?> e : ((Map<?, ?>) scriptResult).entrySet()) {
	            result.put(new SimpleMetricName(e.getKey().toString(), null), 
	                       new MetricValue(System.currentTimeMillis(), e.getValue()));
	        }
	    } else if (scriptResult instanceof List) {
	        for (List<?> e : (List<List<?>>) scriptResult) {
	            if (e.size() == 2) {
	                result.put(new SimpleMetricName(e.get(0).toString(), null), 
	                           new MetricValue(System.currentTimeMillis(), e.get(1)));
	            } else if (e.size() == 3) {
	                result.put(new SimpleMetricName(e.get(0).toString(), null), 
                               new MetricValue(((Date)e.get(2)).getTime(), e.get(1)));
                } else {
                    throw new IllegalArgumentException("list value script return must only contain lists of size 2 or 3!");
                }
            }
	    }
	    
	    
		return result;
	}
}
