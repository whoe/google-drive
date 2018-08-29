package com.company.googledrive.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;

/**
 * Settings for Google Drive file storage integration
 *
 * @author adiatullin
 */
@Source(type = SourceType.APP)
public interface GoogleDriveConfig extends Config {

    /**
     * @return Google application name
     */
    @Property("google.drive.applicationName")
    String getApplicationName();

    /**
     * @return Google drive oauth client json (including client secret and id)
     */
    @Property("google.drive.clientOAuthSecretJson")
    String getClientOAuthSecretJson();

    /**
     * @return refresh authorization token
     */
    @Property("google.drive.refreshToken")
    String getRefreshToken();

    /**
     * @return path of file storage root folder
     */
    @Property("google.drive.fileStorageFolder")
    String getFileStorageFolder();
}
