package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import io.vavr.control.Try;
import io.vavr.control.Option;
import io.vavr.collection.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    
    public Try<Option<User>> findById(Integer userId) {
        return userDAO.findById(userId);
    }
    
 public Try<List<User>> getAllUsers() {
    return userDAO.findAll();
}

public Try<List<User>> findAllActive() {
    return userDAO.findAllActive();
}
}