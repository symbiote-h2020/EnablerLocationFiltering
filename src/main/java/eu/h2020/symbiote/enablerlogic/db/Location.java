/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.enablerlogic.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
/**
 *
 * @author Luca Tomaselli <l.tomaselli@nextworks.it>
 */
@Document(collection="locations")
public class Location {
    
    @Id
    @JsonProperty("id")
    private int id;
    
    @JsonProperty("locationName")
    private String locationName;
    
    @JsonProperty("locationLatitude")
    private double locationLatitude;
    
    @JsonProperty("locationLongitude")
    private double locationLongitude;
    
    @JsonProperty("locationAltitude")
    private double locationAltitude;
    
    @JsonProperty("ParentId")
    private Integer parentId;
    
    @JsonIgnore
    private Location parent;
    
    @JsonIgnore
    private List<Location> children;
    
    
    
    public Location() {
        this.id = -1;
        this.locationName = null;
        this.locationLatitude = -1;
        this.locationLongitude = -1;
        this.locationAltitude = -1;
        this.parentId = null;
    }
    
    @JsonCreator
    public Location(@JsonProperty("id") int id, 
            @JsonProperty("locationName") String locationName,
            @JsonProperty("locationLatitude") double locationLatitude,
            @JsonProperty("locationLongitude") double locationLongitude,
            @JsonProperty("locationAltitude") double locationAltitude,
            @JsonProperty("parentId") Integer parentId) {
        this.id = id;
        this.locationName = locationName;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.locationAltitude = locationAltitude;
        this.parentId = parentId;
    }

    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("locationName")
    public String getLocationName() {
        return locationName;
    }

    @JsonProperty("locationName")
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @JsonProperty("locationLatitude")
    public double getLocationLatitude() {
        return locationLatitude;
    }

    @JsonProperty("locationLatitude")
    public void setLocationLatitude(double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    @JsonProperty("locationLongitude")
    public double getLocationLongitude() {
        return locationLongitude;
    }

    @JsonProperty("locationLongitude")
    public void setLocationLongitude(double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    @JsonProperty("locationAltitude")
    public double getLocationAltitude() {
        return locationAltitude;
    }

    @JsonProperty("locationAltitude")
    public void setLocationAltitude(double locationAltitude) {
        this.locationAltitude = locationAltitude;
    }

    @JsonProperty("ParentId")
    public Integer getParentId() {
        return parentId;
    }

    @JsonProperty("ParentId")
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @JsonIgnore
    public Location getParent() {
        return parent;
    }

    @JsonIgnore
    public void setParent(Location parent) {
        this.parent = parent;
    }

    @JsonIgnore
    public List<Location> getChildren() {
        return children;
    }

    @JsonIgnore
    public void setChildren(List<Location> children) {
        this.children = children;
    }
    
    @Override
    public String toString() {
      return "Location [id=" + id + ", locationName=" + locationName + ", parentId=" + parentId + "]";
    }
}
