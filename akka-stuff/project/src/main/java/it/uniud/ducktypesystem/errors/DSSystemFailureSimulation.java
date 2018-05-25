package it.uniud.ducktypesystem.errors;

public class DSSystemFailureSimulation extends Exception {
    public DSSystemFailureSimulation(Exception e) { super(e); }
    public DSSystemFailureSimulation(String msg) { super(msg); }
}
