package com.mycompany.telegramsimplebot.repository;

import com.mycompany.telegramsimplebot.entity.User;
import org.springframework.data.repository.CrudRepository;
//import org.springframework.stereotype.Repository;


public interface UserRepository extends CrudRepository<User, Long> {
    
}
