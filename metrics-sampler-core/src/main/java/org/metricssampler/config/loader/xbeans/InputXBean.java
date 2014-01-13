package org.metricssampler.config.loader.xbeans;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metricssampler.config.ConfigurationException;
import org.metricssampler.config.InputConfig;

/**
 * Base class for input XBeans.
 */
public abstract class InputXBean extends TemplatableXBean {
	private List<VariableXBean> variables;
	private File configFile;
	
	public List<VariableXBean> getVariables() {
		return variables;
	}

	public void setVariables(final List<VariableXBean> variables) {
		this.variables = variables;
	}

	public InputConfig toConfig() {
		if (isTemplate()) {
			throw new ConfigurationException("Tried to use abstract bean \"" + getName() + "\"");
		}
		validate();
		return createConfig();
	}
	
	protected Map<String, Object> getVariablesConfig() {
		final Map<String, Object> result = new HashMap<String, Object>();
		if (variables != null) {
			for (final VariableXBean item : variables) {
				result.put(item.getName(), item.getValue());
			}
		}
		return result;
	}
	
	protected abstract InputConfig createConfig();

    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }
}
