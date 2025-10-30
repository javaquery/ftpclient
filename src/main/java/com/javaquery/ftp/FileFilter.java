package com.javaquery.ftp;

/**
 * @author javaquery
 * @since 2025-10-30
 */
public interface FileFilter<RemoteFile> {
    boolean accept(RemoteFile file);
}
