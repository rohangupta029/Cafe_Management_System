package com.in.cafe.JWT;

import com.in.cafe.dao.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Service
public class CustomerUserDetailsService implements UserDetailsService {


    @Autowired
    UserDao userDao;

    private com.in.cafe.POJO.User userDetails;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("Inside loadUserByUsername {}", username);

        userDetails =userDao.findByEmailId(username);

        if (!Objects.isNull(userDetails)){
            log.info("Inside loadUserByUsername  and userDetails{}", userDetails);
            return new User(userDetails.getEmail(), userDetails.getPassword(), new ArrayList<>());
        }
        else
            throw new UsernameNotFoundException("User not found");


    }

    public com.in.cafe.POJO.User getUserDetails(){
        return userDetails;
    }
}
