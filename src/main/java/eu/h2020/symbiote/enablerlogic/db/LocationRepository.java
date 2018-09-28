/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.enablerlogic.db;

import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;
/**
 *
 * @author Luca Tomaselli <l.tomaselli@nextworks.it>
 */
@Repository
public interface LocationRepository extends MongoRepository<Location, String>,LocationRepositoryCustom {
    
    public Optional<Location> findById(int id);
    

    public Optional<Location> findByLocationName(String locationName,List<String> platformId);
    
    @Override
    public List<Location> findAll();
    
    @Override
    public Location save(Location l);
}
