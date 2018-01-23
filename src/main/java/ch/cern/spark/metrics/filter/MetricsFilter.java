package ch.cern.spark.metrics.filter;

import java.io.Serializable;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.cern.properties.ConfigurationException;
import ch.cern.properties.Properties;
import ch.cern.spark.metrics.Metric;
import ch.cern.util.function.AndPredicate;
import ch.cern.util.function.OrPredicate;
import lombok.ToString;

@ToString
public class MetricsFilter implements Predicate<Metric>, Serializable{
    
    private static final long serialVersionUID = 9170996730102744051L;

    private Predicate<Metric> predicate = null;
    
    public MetricsFilter(){
    }
    
    @Override
	public boolean test(Metric metric) {
    		if(predicate == null)
    			return true;
    		
		return predicate.test(metric);
	}
    
    public void addPredicate(String key, String value) throws ParseException{
    		if(value.charAt(0) == '!')  		
        		addPredicate(new NotEqualMetricPredicate(key, value.substring(1)));
    		else
        		addPredicate(new EqualMetricPredicate(key, value));
    }

    public void addPredicate(Predicate<Metric> newPredicate) {
    		if(predicate == null)
    			predicate = newPredicate;
    		else
    			predicate = new AndPredicate<Metric>(predicate, newPredicate);
	}

	public static MetricsFilter build(Properties props) throws ConfigurationException {
		MetricsFilter filter = new MetricsFilter();
        
        String expression = props.getProperty("expr");
        if(expression != null)
			try {
				filter.addPredicate(MetricPredicateParser.parse(expression));
			} catch (ParseException e) {
				throw new ConfigurationException("Error when parsing filter expression: " + e.getMessage());
			}
        
        Properties filterProperties = props.getSubset("attribute");
        
        try {
            for (Entry<Object, Object> attribute : filterProperties.entrySet()) {
                String key = (String) attribute.getKey();
                String valueString = (String) attribute.getValue();
                
                List<String> values = getValues(valueString);
                if(values.size() == 0) {
            			try {
            				filter.addPredicate(key, valueString);
            			} catch (ParseException e) {
            				throw new ConfigurationException("Error when parsing filter (" + key + ") value expression (" + valueString + "): " + e.getMessage());
            			}
                }else {
                    boolean negate = valueString.startsWith("!");
                    
                    if(negate)
                        for (String value : values)
                            filter.addPredicate(new NotEqualMetricPredicate(key, value));
                    else {
                        Predicate<Metric> orOptions = null;
                        
                        for (String value : values)
                            if(orOptions  == null)
                                orOptions = new EqualMetricPredicate(key, value);
                            else
                                orOptions = new OrPredicate<Metric>(orOptions, new EqualMetricPredicate(key, value));
                        
                        filter.addPredicate(orOptions);
                    }
                }
            }
        
			props.confirmAllPropertiesUsed();
		} catch (ConfigurationException|ParseException e) {
		    throw new ConfigurationException("Error when parsing filter: " + e.getMessage());
        }
        
        return filter;
    }
    
	private static List<String> getValues(String valueString) {
	    Pattern pattern = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
	    
	    LinkedList<String> hits = new LinkedList<>();
	    
        Matcher m = pattern.matcher(valueString);
        while (m.find()) {
            String value = m.group();
            
            value = value.substring(1, value.length() - 1);
            
            hits.add(value);
        }
	    
        return hits;
    }
    
}