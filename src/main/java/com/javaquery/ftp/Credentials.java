package com.javaquery.ftp;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author javaquery
 * @since 2025-10-30
 */
@Getter
@Setter
@Builder
public class Credentials {
    private String host;
    private int port;
    private String username;
    private String password;
    private int connectTimeout = 5000;
    private int socketTimeout = 5000;
    private boolean isImplicit;
}
