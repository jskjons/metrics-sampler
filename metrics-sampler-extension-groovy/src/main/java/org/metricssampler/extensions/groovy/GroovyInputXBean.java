package org.metricssampler.extensions.groovy;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.metricssampler.config.loader.xbeans.ArgumentXBean;
import org.metricssampler.config.loader.xbeans.InputXBean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("groovy")
public class GroovyInputXBean extends InputXBean {
	@XStreamAsAttribute
	private String scriptFile;
	private String script;
	
	private List<ArgumentXBean> arguments;
	

	public List<ArgumentXBean> getArguments() {
		return arguments;
	}

	public void setArguments(final List<ArgumentXBean> arguments) {
		this.arguments = arguments;
	}

	@Override
	protected void validate() {
		super.validate();
		if (scriptFile == null && script == null) {
            throw new IllegalArgumentException("either a script element or scriptFile must be specified!");
        }
		if (arguments != null && !arguments.isEmpty()) {
			for (final ArgumentXBean argument : arguments) {
				argument.validate();
			}
		}
	}

	@Override
	protected GroovyInputConfig createConfig() {
		return new GroovyInputConfig(getName(), getVariablesConfig(), getScriptFile(), script, getArgumentsConfig());
	}

    private File getScriptFile() {
        return scriptFile != null ? new File(getConfigFile().getParentFile(), scriptFile) : null;
    }

	protected List<String> getArgumentsConfig() {
		final List<String> result = new LinkedList<String>();
		if (arguments != null) {
			for (final ArgumentXBean argument : arguments) {
				result.add(argument.getValue());
			}
		}
		return result;
	}
}
