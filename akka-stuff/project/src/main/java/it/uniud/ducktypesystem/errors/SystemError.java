package it.uniud.ducktypesystem.errors;

public class SystemError extends Exception {
    public SystemError(Throwable t) { super(t); }
    public SystemError(String msg) { super(msg); }
}
