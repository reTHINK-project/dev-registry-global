package com.MVC.model;

/**
 * Created by Half-Blood on 1/4/2017.
 */
public class DatasetIntegrityException extends Exception{

    private static final long serialVersionUID = 8787254843105440320L;

    public DatasetIntegrityException(String message)
    {
        super(message);
    }

    public DatasetIntegrityException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DatasetIntegrityException(Throwable cause)
    {
        super(cause);
    }
}