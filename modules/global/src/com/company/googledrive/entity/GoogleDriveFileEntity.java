package com.company.googledrive.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.chile.core.annotations.MetaProperty;

@MetaClass(name = "googledrive$GoogleDriveFileEntity")
public class GoogleDriveFileEntity extends BaseUuidEntity {
    private static final long serialVersionUID = 6802857655118275763L;

    @MetaProperty
    protected GoogleDriveFileEntity parent;

    @MetaProperty
    protected String ext;

    @MetaProperty
    protected String kind;

    @MetaProperty
    protected String parents;

    @MetaProperty
    protected String mime;

    @MetaProperty
    protected String name;

    @MetaProperty
    protected String fileId;

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getKind() {
        return kind;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public String getParents() {
        return parents;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getMime() {
        return mime;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }


    public void setParent(GoogleDriveFileEntity parent) {
        this.parent = parent;
    }

    public GoogleDriveFileEntity getParent() {
        return parent;
    }


}