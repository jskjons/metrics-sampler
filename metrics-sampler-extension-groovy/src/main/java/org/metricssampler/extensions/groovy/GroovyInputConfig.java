package org.metricssampler.extensions.groovy;

import static org.metricssampler.util.Preconditions.checkArgumentNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.metricssampler.config.InputConfig;

public class GroovyInputConfig extends InputConfig {
	private final String scriptText;
	private final List<String> arguments;

	public GroovyInputConfig(final String name, final Map<String, Object> variables, final File scriptFile, final String script,
			final List<String> arguments) {
		super(name, variables);
		checkArgumentNotNull(arguments, "arguments");
		
		if (scriptFile == null && script == null) {
		    throw new IllegalArgumentException("either a script element or scriptFile must be specified!");
		}
		this.arguments = arguments;
		scriptText = scriptFile != null ? readScriptFile(scriptFile) : script;;
	}

    public String getScriptText() {
        return scriptText; 
    }

    public List<String> getArguments() {
        return arguments;
    }
    
    private String readScriptFile(File file) {
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new IllegalArgumentException("unable to read file: " + file);
        }
    }
}