package ch.cern.spark.metrics.defined;

import java.util.Map;
import java.util.Optional;

import org.apache.spark.streaming.State;

import ch.cern.properties.Properties;
import ch.cern.spark.metrics.Metric;
import ch.cern.spark.metrics.defined.equation.var.VariableStatuses;
import ch.cern.spark.status.UpdateStatusFunction;

public class UpdateDefinedMetricStatusesF extends UpdateStatusFunction<DefinedMetricStatuskey, Metric, VariableStatuses, Metric>{

	private static final long serialVersionUID = 2965182980222300453L;

	private Properties propertiesSourceProps;

	public UpdateDefinedMetricStatusesF(Properties propertiesSourceProps) {
		this.propertiesSourceProps = propertiesSourceProps;
	}
	
    @Override
    protected Optional<Metric> update(DefinedMetricStatuskey id, Metric metric, State<VariableStatuses> status) 
            throws Exception {
        Optional<DefinedMetric> definedMetricOpt = getDefinedMetric(id.getID());
        if(!definedMetricOpt.isPresent()) {
            status.remove();
            return Optional.empty();
        }
        DefinedMetric definedMetric = definedMetricOpt.get();
            
        VariableStatuses store = getStore(status);

        definedMetric.updateStore(store, metric, id.getMetric_attributes().keySet());
        
        Optional<Metric> newMetric = definedMetric.generateByUpdate(store, metric, id.getMetric_attributes());
        
        status.update(store);
        
        return newMetric;
    }

	protected Optional<DefinedMetric> getDefinedMetric(String id) throws Exception {
	    DefinedMetrics.initCache(propertiesSourceProps);
	    
        Map<String, DefinedMetric> definedMetrics = DefinedMetrics.getCache().get();
        
        return Optional.ofNullable(definedMetrics.get(id));
    }

    private VariableStatuses getStore(State<VariableStatuses> status) {
		return status.exists() ? status.get() : new VariableStatuses();
	}

}
