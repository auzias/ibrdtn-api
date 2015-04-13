package org.ibrdtnapi;

public class ApiException extends RuntimeException {
	private static final long serialVersionUID = -5659984882961407662L;
	
	public ApiException(String msg) {
		super(msg);
	}
}
