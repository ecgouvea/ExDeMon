package ch.cern.spark.metrics.value;

import java.time.Instant;
import java.util.Optional;

import ch.cern.spark.metrics.defined.equation.ValueComputable;
import ch.cern.spark.metrics.defined.equation.var.VariableStatuses;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
public class StringValue extends Value implements ValueComputable{

	private static final long serialVersionUID = 6026199196915653369L;

	private String str;
	
	public StringValue(String value){
		this.str = value;
	}

	@Override
	public Optional<String> getAsString() {
		return Optional.of(str);
	}

	@Override
	public StringValue compute(VariableStatuses store, Instant time) {
		return new StringValue(str);
	}
	
	@Override
	public Class<StringValue> returnType() {
		return StringValue.class;
	}
	
	@Override
	public String toString() {
		return "\"" + str + "\"";
	}
	
	@Override
	public String getSource() {
		if(source == null)
			return toString();
		else
			return super.toString();
	}

}
