package org.metricssampler.extensions.jdbc;

import static org.metricssampler.config.loader.xbeans.ValidationUtils.notEmpty;

import java.util.List;

import org.metricssampler.config.InputConfig;
import org.metricssampler.config.loader.xbeans.InputXBean;
import org.metricssampler.util.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("jdbc")
public class JdbcInputXBean extends InputXBean {
	@XStreamAsAttribute
	private String pool;

	@XStreamAsAttribute
	@XStreamAlias("shared-queries")
    private String sharedQueries;
	
	@XStreamImplicit(itemFieldName="query")
	private List<String> queries;
	
	@Override
	protected void validate() {
		super.validate();
		notEmpty(this, "pool", getPool());
		if (StringUtils.isEmptyOrNull(sharedQueries)) {
		    notEmpty(this, "queries", getQueries());
		}
	}

	@Override
	protected InputConfig createConfig() {
		return new JdbcInputConfig(getName(), getVariablesConfig(), getPool(), getQueries(), sharedQueries);
	}

	
	public String getPool() {
		return pool;
	}

	public void setPool(final String pool) {
		this.pool = pool;
	}

	public List<String> getQueries() {
		return queries;
	}

	public void setQueries(final List<String> queries) {
		this.queries = queries;
	}

    public String getSharedQueries() {
        return sharedQueries;
    }

    public void setSharedQueries(String sharedQueries) {
        this.sharedQueries = sharedQueries;
    }
}
