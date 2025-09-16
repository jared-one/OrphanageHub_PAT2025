package com.orphanagehub.service;

import com.orphanagehub.dao.UserDAO;
import com.orphanagehub.model.User;
import com.orphanagehub.util.PasswordUtil;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.time.LocalDateTime;
import java.util.Map;

public class RegistrationService {

    private final UserDAO userDAO = new UserDAO();

    public Try<User> registerWithExpandedData(Map<String, Object> data, String password) {
        return Try.of(() -> {
            String username = (String) data.get("username");
            String email = (String) data.get("email");
            String fullName = (String) data.get("fullName");
            String phone = (String) data.get("phone");
            String province = (String) data.get("province");
            String role = (String) data.get("role");

            User newUser = new User(
                null, // userId is auto-generated
                username,
                PasswordUtil.hash(password.toCharArray()),
                email,
                role,
                LocalDateTime.now(),
                Option.none(), // lastLogin
                Option.of(fullName),
                Option.of(phone),
                Option.none(), // idNumber
                Option.none(), // dateOfBirth
                Option.none(), // address
                Option.none(), // city
                Option.of(province),
                Option.none(), // postalCode
                "Active", // accountStatus
                false, // emailVerified
                Option.none(), // verificationToken
                Option.none(), // passwordResetToken
                Option.none(), // passwordResetExpiry
                Option.none(), // profilePicture
                Option.none(), // bio
                Option.none(), // createdBy
                Option.none(), // modifiedDate
                Option.none() // modifiedBy
            );
            return newUser;
        }).flatMap(user -> userDAO.create(user));
    }

    public Try<Boolean> isUsernameAvailable(String username) {
        return userDAO.findByUsername(username).map(opt -> opt.isEmpty());
    }

    public Try<Boolean> isEmailAvailable(String email) {
        // Assume add findByEmail to UserDAO
        return Try.success(true); // Stub; implement similarly
    }
}