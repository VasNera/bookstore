package com.neratzis.bookstore.mapper;


import com.neratzis.bookstore.dto.UserInsertDTO;
import com.neratzis.bookstore.dto.UserReadOnlyDTO;
import com.neratzis.bookstore.dto.UserUpdateDTO;
import com.neratzis.bookstore.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    public User mapToUserEntity(UserInsertDTO userInsertDTO){
      User user = new User();

      user.setUsername(userInsertDTO.username());
      user.setPassword(userInsertDTO.password());
      user.setFirstname(userInsertDTO.firstname());
      user.setLastname(userInsertDTO.lastname());
      user.setEmail(userInsertDTO.email());
      user.setPhone(userInsertDTO.phone());

      return user;
    }

    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user){

        return new UserReadOnlyDTO(
                user.getUuid(),
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getName()
        );
    }

    public void updateUserFromDTO(User user, UserUpdateDTO userUpdateDTO){
        user.setFirstname(userUpdateDTO.firstname());
        user.setLastname(userUpdateDTO.lastname());
        user.setEmail(userUpdateDTO.email());
        user.setPhone(userUpdateDTO.phone());
    }
}
