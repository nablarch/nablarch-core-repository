package nablarch.core.repository.di.test;

import nablarch.core.repository.IgnoreProperty;

public class IgnorePropertyBean {

    @IgnoreProperty("廃止されました。")
    public void setIgnore(String value) {
    }
    
    public void setValid(String value) {
        
    }
}
