package com.javaquery.ftp.exception;

/**
 * Custom FTP exception for the library
 *
 * @author javaquery
 * @since 1.0.0
 */
public class FTPException extends RuntimeException{

    public FTPException(String message, Exception e){
        super(message, e);
    }
}
