/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.enablerlogic.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 *
 * @author Luca Tomaselli <l.tomaselli@nextworks.it>
 */
public class LocationRepositoryImpl implements LocationRepositoryCustom{

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public Location save(Location location) throws Exception{
        Optional<Location> locationOld = findByLocationName(location.getLocationName(),location.getPlatformId());
        if(locationOld != null && locationOld.isPresent())
            throw new Exception("Location already present with this name");
        if(location.getId() <= 0){
            int newId = 0;
            Query query = new Query();
            query.with(new Sort(Sort.Direction.DESC, "id"));
            query.limit(1);
            Location maxLocation = mongoTemplate.findOne(query, Location.class);
            if(maxLocation != null)
                newId = maxLocation.getId();
            location.setId(newId + 1);
        }
        mongoTemplate.save(location);
        return location;
    }

    @Override
    public List<Location> getLocationChildren(String locationName,String platformId) throws Exception{
        List<Location> children;
        Optional<Location> locationStartOptional = findByLocationName(locationName,platformId);
        if(locationStartOptional == null || !locationStartOptional.isPresent())
            throw new Exception("Not exist Location with this name");
        
        Location locationStart = locationStartOptional.get();
        children = getLocationChildrenRecursive(locationStart,new ArrayList<Location>());
        if(children == null)
            children = new ArrayList<Location>();
        children.add(locationStart);
        return children;
    }
    
    private List<Location> getLocationChildrenRecursive(Location location, List<Location> visited){
        List<Location> locationChildren = new ArrayList<>();
        List<Location> locationChildrenNew = getLocationChildren(location.getId());
        if(locationChildrenNew.isEmpty())
            locationChildren = locationChildrenNew;
        else{
            for(Location locationNew: locationChildrenNew){
                List<Location> locationChildrenNew2 = getLocationChildrenRecursive(locationNew,visited);
                locationChildren.addAll(locationChildrenNew2);
                locationChildren.add(locationNew);
            }
        }
        return locationChildren;
    }
    
    /*private List<Location> getLocationChildrenRecursive(Location location, List<Location> visited){
        List<Location> locationChildren = new ArrayList<>();
        if(!visited.contains(location)){
            visited.add(location);
            List<Location> locationChildrenNew = getLocationChildren(location.getId());
            locationChildren.addAll(locationChildrenNew);
            for(Location locationNew: locationChildren){
                List<Location> locationChildrenNew2 = getLocationChildrenRecursive(locationNew,visited);
                locationChildren.addAll(locationChildrenNew2);
            } 
        }
        return locationChildren;
    }*/
    
    private List<Location> getLocationChildren(int parentId){
        List<Location> locationChildren;
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(parentId));
        locationChildren = mongoTemplate.find(query, Location.class);
        if(locationChildren == null)
            locationChildren = new ArrayList<>();
        return locationChildren;
    }

    @Override
    public List<Location> getLocationStructure(String platformId) {
        List<Location> locationStarter;
        Query query = new Query();
        query.addCriteria(Criteria.where("parentId").is(null));
        query.addCriteria(Criteria.where("platformId").is(platformId));
        locationStarter = mongoTemplate.find(query, Location.class);
        for(Location l: locationStarter){
            l = getLocationStructureRecursive(l);
        }
        return locationStarter;
    }
    
    public Location getLocationStructureRecursive(Location l) {
        //List<Location> locationChildren = new ArrayList<>();
        List<Location> locationChildrenNew = getLocationChildren(l.getId());
        l.setChildren(locationChildrenNew);
        for(Location locationNew: locationChildrenNew){
            locationNew = getLocationStructureRecursive(locationNew);
            locationNew.setParent(l);
        }
        return l;
    }

    @Override
    public Optional<Location> findByLocationName(String locationName,String platformId) {
        Optional<Location> locationOptional = null;
        Query query = new Query();
        query.addCriteria(Criteria.where("locationName").is(locationName));
        query.addCriteria(Criteria.where("platformId").is(platformId));
        Location location = mongoTemplate.findOne(query, Location.class);
        if(location != null)
            locationOptional = Optional.of(location);
        return locationOptional;
    }
    
    @Override
    public Optional<Location> updateParentId(int locationId, Integer parentId) {
        Optional<Location> locationOptional = null;
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(locationId));
        Update update = new Update();
        update.set("parentId", parentId);
        Location location = mongoTemplate.findAndModify(query, update, Location.class);
        if(location != null)
            locationOptional = Optional.of(location);
        return locationOptional;
    }

    @Override
    public void insertFake() {
        /*for(int i = 1;i<=5;i++){
            Location l = new Location(0,"Location"+i,23.23,23.23,69.69,null,"NXW-symphony-1");
            if(i==2 || i==3)
                l.setParentId(1);
            if(i==4)
                l.setParentId(2);
            if(i==5)
                l.setParentId(3);
            try {
                save(l);
            } catch (Exception ex) {
                Logger.getLogger(LocationRepositoryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        */
        Location l = new Location(0,"Pisa",43.6816,10.3531,1,null,"NXW-symphony-1");
        try {
            save(l);
        } catch (Exception ex) {
            Logger.getLogger(LocationRepositoryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        Location l2 = new Location(0,"Viareggio",43.8625,10.2425,1,null,"NXW-symphony-1");
        try {
            save(l2);
        } catch (Exception ex) {
            Logger.getLogger(LocationRepositoryImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
