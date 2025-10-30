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
    @Builder.Default
    private int connectTimeout = 15000;
    @Builder.Default
    private int socketTimeout = 60000;
    private boolean isImplicit;
}
