package com.javaquery.ftp.io;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

/**
 * @author javaquery
 * @since 2025-10-30
 */
@Getter
@Setter
@Builder
public class RemoteFile {
    private String name;
    private boolean isFile;
    private boolean isDirectory;
    private long size;
    private Calendar timestamp;
    private String path;
}
