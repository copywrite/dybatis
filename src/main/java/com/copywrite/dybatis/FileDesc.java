package com.copywrite.dybatis;

public class FileDesc {

    private String path = null;
    private long tm;
    private String md5;

    public FileDesc(String path, long tm, String md5) {
        super();
        this.path = path;
        this.tm = tm;
        this.md5 = md5;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTm() {
        return tm;
    }

    public void setTm(long tm) {
        this.tm = tm;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FileDesc && this.path.equals(((FileDesc) o).path) && this.tm == ((FileDesc) o).tm;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 37 + (int) (tm ^ (tm >>> 32));
        result = result * 37 + this.path.hashCode();
        return result;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
