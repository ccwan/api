package api;

public class ClassField {
	String paramName;
	String paramType;
	
	public static final String DATA_TYPE_STRING = "string";
	public static final String DATA_TYPE_NUMBER = "number";
	public static final String DATA_TYPE_BOOLEAN = "boolean";

	public ClassField() {
	}
	
	public ClassField(String paramName, String paramType) {
		this.paramName = paramName;
		this.paramType = paramType;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}
}
