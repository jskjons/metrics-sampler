package org.metricssampler.extensions.groovy;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.metricssampler.reader.MetricName;
import org.metricssampler.reader.MetricValue;

public class GroovyMetricsReaderTest {

    @Test
    public void canReadMetricsMapFromGroovyScript() {
        Map<MetricName, MetricValue> metrics = readFromScript("return [foo: args[0]]", "1").readAllMetrics();
        assertEquals(1, metrics.size());
        assertMetric(metrics, "foo", "1");
    }
    
    @Test
    public void canReadMetricsListFromGroovyScript() {
        Map<MetricName, MetricValue> metrics = readFromScript("return [['foo', 1, new Date(100)]]").readAllMetrics();
        assertEquals(1, metrics.size());
        MetricValue value = assertMetric(metrics, "foo", 1);
        assertEquals(100, value.getTimestamp());
        
        metrics = readFromScript("return [['foo', 1]]").readAllMetrics();
        assertEquals(1, metrics.size());
        assertMetric(metrics, "foo", 1);
    }

    private MetricValue assertMetric(Map<MetricName, MetricValue> metrics, String name, Object value) {
        MetricName metricName = metrics.keySet().iterator().next();
        assertEquals(name, metricName.getName());
        assertEquals(value, metrics.get(metricName).getValue());
        return metrics.get(metricName);
    }
    
    private GroovyMetricsReader readFromScript(String text, String... args) {
        return new GroovyMetricsReader(new GroovyInputConfig("groovy", new HashMap<String, Object>(), null, text, Arrays.asList(args)));
    }
}
