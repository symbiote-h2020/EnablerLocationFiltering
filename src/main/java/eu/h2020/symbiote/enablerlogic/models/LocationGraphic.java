/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.enablerlogic.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author Luca Tomaselli <l.tomaselli@nextworks.it>
 */
public class LocationGraphic {
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("children")
    private List<LocationGraphic> children;

    
    public LocationGraphic(){
        id = -1;
        text = null;
        children = null;
    }
    
    public LocationGraphic(@JsonProperty("id") int id, 
            @JsonProperty("text") String text,
            @JsonProperty("children") List<LocationGraphic> children){
        this.id = id;
        this.text = text;
        this.children = children;
    }
    
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("text")
    public String getText() {
        return text;
    }

    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("children")
    public List<LocationGraphic> getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(List<LocationGraphic> children) {
        this.children = children;
    }
    
    
}
