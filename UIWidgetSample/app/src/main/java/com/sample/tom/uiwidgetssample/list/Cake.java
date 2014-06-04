package com.sample.tom.uiwidgetssample.list;

import java.io.Serializable;

/**
* Created by TomReinhart on 6/4/14.
*/
public class Cake implements Serializable {
    private String name;
    private String description;
    private String url;

    public Cake(String _name, String _description, String _url) {
        this.name = _name;
        this.description = _description;
        this.url = _url;
    }
    public String getName() {
        return this.name;
    }
    public String getDescription() {
        return this.description;
    }
    public String getUrl() {
        return this.url;
    }
}
