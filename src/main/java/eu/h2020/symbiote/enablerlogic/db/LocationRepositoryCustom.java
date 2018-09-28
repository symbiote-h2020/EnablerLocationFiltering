/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.enablerlogic.db;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Luca Tomaselli <l.tomaselli@nextworks.it>
 */
public interface LocationRepositoryCustom {
    
    public Boolean deleteFromPlatformId(String platformId);
    
    public abstract Location save(Location location) throws Exception;
    
    public abstract List<Location> getLocationChildren (String locationName,List<String> platformId) throws Exception;
    
    public abstract List<Location> getLocationStructure(String platformId);
    
    public abstract Optional<Location> findByLocationName(String locationName,List<String> platformId);
    
    public abstract Optional<Location> updateParentId(int locationId, Integer parentId);
    
    //public boolean 
    
    public abstract void insertFake();
}
