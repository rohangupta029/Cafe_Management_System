package com.in.cafe.serviceImpl;

import com.in.cafe.JWT.CustomerUserDetailsService;
import com.in.cafe.JWT.JwtFilter;
import com.in.cafe.JWT.JwtUtils;
import com.in.cafe.POJO.User;
import com.in.cafe.constant.CafeConstant;
import com.in.cafe.dao.UserDao;
import com.in.cafe.service.UserService;
import com.in.cafe.utils.CafeUtils;
import com.in.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    JwtUtils jwtUtils;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside SignUp {}", requestMap);
        try {


            if (validateSignUpMap(requestMap)) {
                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Successfully Registered.", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("Email already exits", HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login method");
        try {
            Authentication auth= authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );

            if (auth.isAuthenticated()){

                if (customerUserDetailsService.getUserDetails().getStatus().equalsIgnoreCase("true")){
                    return new ResponseEntity<String>("{\"token\":\"" + jwtUtils. generateToken(customerUserDetailsService.getUserDetails().getEmail(),
                            customerUserDetailsService.getUserDetails().getRole()) + "\"}", HttpStatus.OK);
                }
                else {
                    return  CafeUtils.getResponseEntity(CafeConstant.WAIT, HttpStatus.BAD_REQUEST);
                }

            }


        }catch (Exception ex){
            log.error("{}", ex);
        }

        return CafeUtils.getResponseEntity(CafeConstant.BAD_CREDENTIALS, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
                if (jwtFilter.isAdmin()){

                    return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);

                }
                else {
                    return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
                }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {

                Optional<User> user = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if (user.isPresent()) {

                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));

                    return CafeUtils.getResponseEntity("User status updated successfully", HttpStatus.OK);

                } else {
                    return CafeUtils.getResponseEntity("User id doesn't exist", HttpStatus.OK);
                }

            } else {
                return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORISED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignUpMap(Map<String, String> requestmap){
        if (requestmap.containsKey("name") && requestmap.containsKey("contactNumber") && requestmap.containsKey("email") && requestmap.containsKey("password"))
        {
            return true;
        }
        else return false;

    }

    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setPassword(requestMap.get("password"));
        user.setEmail(requestMap.get("email"));
        user.setRole("user");
        user.setStatus("false");
        return user;
    }
}
