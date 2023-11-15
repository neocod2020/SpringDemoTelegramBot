package com.mycompany.telegramsimplebot.service;

import com.mycompany.telegramsimplebot.entity.User;
import com.mycompany.telegramsimplebot.repository.UserRepository;

public interface UserService extends UserRepository {
    public User getById(Long id);
    
}
