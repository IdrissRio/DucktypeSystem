package it.uniud.ducktypesystem.distributed.errors;

/**
 * Exception used to signal internal system errors.
 */
public class DSSystemError extends Exception {
    public DSSystemError(Throwable t) { super(t); }
    public DSSystemError(String msg) { super(msg); }
}
