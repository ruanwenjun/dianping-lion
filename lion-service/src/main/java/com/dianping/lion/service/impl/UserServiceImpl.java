/**
 * Project: com.dianping.lion.lion-console-0.0.1
 * 
 * File Created at 2012-7-12
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.lion.service.impl;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.lion.dao.UserDao;
import com.dianping.lion.entity.User;
import com.dianping.lion.exception.EntityNotFoundException;
import com.dianping.lion.exception.IncorrectPasswdException;
import com.dianping.lion.exception.SystemUserForbidLoginException;
import com.dianping.lion.exception.UserLockedException;
import com.dianping.lion.exception.UserNotFoundException;
import com.dianping.lion.service.LDAPAuthenticationService;
import com.dianping.lion.service.UserService;

public class UserServiceImpl implements UserService {
	
	@Autowired
	private LDAPAuthenticationService ldapAuthenticationService;
	@Autowired
	private UserDao userDao;
	
	@Override
	public List<User> findAll() {
		return userDao.findAll();
	}

	@Override
	public User findById(int id) {
		return userDao.findById(id);
	}

    @Override
    public User loadById(int id) {
        User user = findById(id);
        if (user == null) {
            throw new EntityNotFoundException("User[id=" + id + "] not found.");
        }
        return user;
    }

    /**
     * No need to authenticate in LDAP in three conditions:
     * <ul>
     * <li>User is locked.
     * <li>System user.
     * <li>User authenticated last time.
     * </ul>
     * then go to ldap for authentication, if authenticated and mysql doesn't contains it or with wrong password, inserting or updating
     * the right authenticated account into <br>mysql for authorization and returning it, otherwise, 
     * returning null.
     */
    @Override
    public User login(String loginName, String passwd) {
    	User dbUser = userDao.findByName(loginName);
    	boolean isUpdateNeeded = false;
    	if(dbUser != null) {
	    	if(dbUser.isLocked()) {
	    		throw new UserLockedException();
	    	} else if(dbUser.isSystem()) {
	    		throw new SystemUserForbidLoginException();
	    	} else if(dbUser.getPassword() != null) {
	    		//固有用户mysql验证
	    		if(dbUser.getPassword().equals(DigestUtils.md5Hex(passwd).toUpperCase())) {
	    			return dbUser;
	    		} else {
	    			isUpdateNeeded = true;
//	    			throw new IncorrectPasswdException();
	    		}
	    	}
    	}
    	//
    	User user = ldapAuthenticationService.authenticate(loginName, passwd);
    	if(user != null) {
    		user.setPassword(DigestUtils.md5Hex(passwd).toUpperCase());
    		if(dbUser == null) {
    			//insert the user
    			userDao.insertUser(user);
    		} else if(isUpdateNeeded) {
    			userDao.updatePassword(user);
    		}
    		return user;
    	} else if(isUpdateNeeded) {
    		throw new IncorrectPasswdException();
    	} else {
    		throw new UserNotFoundException(loginName);
    	}
    }

}
