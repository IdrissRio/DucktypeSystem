package it.uniud.ducktypesystem.distributed.errors;

/**
 * Exception used to simulate critical failures,
 * and monitor the system behaviour and fault resilience.
 */
public class DSSystemFailureSimulation extends Exception {
    public DSSystemFailureSimulation(Exception e) { super(e); }
    public DSSystemFailureSimulation(String msg) { super(msg); }
}
