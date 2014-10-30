package com.yiqingart.www.fs;

import java.io.OutputStream;
import java.util.List;

public class CacheOperation {
    private CacheOperation next;
    private Boolean save;
    public CacheOperation(CacheOperation next, Boolean save) {
        super();
        this.next = next;
        this.save = save;
    }
    public Boolean do_save(String filename, List<byte[]> value ){
        return true;
    }
    public List<byte[]> do_get(String filename, OutputStream os ){
        return null;
    }
    public CacheOperation getNext(){
        return next;
    }
    public Boolean IsSave() {
        return save;
    }
}
