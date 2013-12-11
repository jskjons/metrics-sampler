package org.metricssampler.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metricssampler.util.Preconditions;

public class SharedObjectRegistry<T> implements SharedResource {

    private String name;
    private List<T> objects;

    public SharedObjectRegistry(String name, List<T> objects) {
        this.name = name;
        Preconditions.checkArgumentNotNull(objects, "objects");
        this.objects = objects;
    }
    
    @Override
    public void shutdown() {}

    @Override
    public void startup() {}

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<String, Object>();
        stats.put("registry." + name + ".size", getObjects().size());
        return stats;
    }

    public List<T> getObjects() {
        return objects;
    }

    public void setObjects(List<T> objects) {
        this.objects = objects;
    }

}
