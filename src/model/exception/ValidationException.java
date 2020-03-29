package model.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = -2842733980671335121L;
	
	private Map<String,String> errors = new HashMap<>();
	
	public ValidationException(String mensagem) {
		super(mensagem);
	}
	
	public Map<String, String> getErrors() {
		return errors;
	}
	
	public void addError(String fieldName, String errorMessage) {
		errors.put(fieldName, errorMessage);
	}

}
