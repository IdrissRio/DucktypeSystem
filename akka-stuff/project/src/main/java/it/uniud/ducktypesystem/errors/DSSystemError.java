package it.uniud.ducktypesystem.errors;

public class DSSystemError extends Exception {
    public DSSystemError(Throwable t) { super(t); }
    public DSSystemError(String msg) { super(msg); }
}
