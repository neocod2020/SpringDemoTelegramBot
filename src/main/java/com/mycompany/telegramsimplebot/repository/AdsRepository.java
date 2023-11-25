package com.mycompany.telegramsimplebot.repository;

import com.mycompany.telegramsimplebot.entity.Ads;
import org.springframework.data.repository.CrudRepository;



public interface AdsRepository extends CrudRepository<Ads, Long> {
    
}
