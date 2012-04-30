package com.crossconnect.model;

import java.net.URI;

/**
 * Bean class for the resource repositories
 * @author glo1
 *
 */
public class ResourceRespository {
    private String churchName;
    private String description;
    private URI icon;
    
    
    public String getChurchName() {
        return churchName;
    }
    public void setChurchName(String churchName) {
        this.churchName = churchName;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public URI getIcon() {
        return icon;
    }
    public void setIcon(URI icon) {
        this.icon = icon;
    }
    
}
