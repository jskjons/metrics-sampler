package org.metricssampler.extensions.jdbc;

import java.util.List;

import org.metricssampler.config.SharedResourceConfig;
import org.metricssampler.util.Preconditions;

public class JdbcSharedQueriesConfig extends SharedResourceConfig {

    private List<String> queries;

    public JdbcSharedQueriesConfig(String name, List<String> queries, boolean ignored) {
        super(name, ignored);
        Preconditions.checkArgumentNotNullNorEmpty(queries, name);
        this.queries = queries;
    }

    public List<String> getQueries() {
        return queries;
    }
}
