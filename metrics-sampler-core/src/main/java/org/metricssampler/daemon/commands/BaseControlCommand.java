package org.metricssampler.daemon.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseControlCommand implements ControlCommand {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final BufferedReader reader;
	protected final BufferedWriter writer;

	protected BaseControlCommand(final BufferedReader reader, final BufferedWriter writer) {
		this.reader = reader;
		this.writer = writer;
	}

	protected void respond(final String line) throws IOException {
		writer.write(line);
		writer.write('\n');
		writer.flush();
	}
}
