package org.metricssampler.extensions.jdbc;

import static org.metricssampler.config.loader.xbeans.ValidationUtils.notEmpty;

import java.util.List;

import org.metricssampler.config.SharedResourceConfig;
import org.metricssampler.config.loader.xbeans.SharedResourceXBean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("jdbc-shared-queries")
public class JdbcSharedQueriesXBean extends SharedResourceXBean {
    @XStreamImplicit(itemFieldName="query")
    private List<String> queries;
    
    @Override
    protected void validate() {
        super.validate();
        notEmpty(this, "queries", getQueries());
    }

    @Override
    protected SharedResourceConfig createConfig() {
        return new JdbcSharedQueriesConfig(getName(), getQueries(), isIgnored());
    }

    public List<String> getQueries() {
        return queries;
    }

    public void setQueries(final List<String> queries) {
        this.queries = queries;
    }
}
