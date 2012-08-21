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
package com.dianping.lion.dao.ibatis;

import java.util.List;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.dianping.lion.dao.UserDao;
import com.dianping.lion.entity.User;

/**
 * @author danson.liu
 *
 */
public class UserIbatisDao extends SqlMapClientDaoSupport implements UserDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findAll() {
		return getSqlMapClientTemplate().queryForList("User.findAll");
	}
	
	public User findById(int id) {
		return (User)getSqlMapClientTemplate().queryForObject("User.findById", id);
	}

	@Override
	public User findByName(String userName) {
		return (User)getSqlMapClientTemplate().queryForObject("User.findByName", userName);
	}

	@Override
	public void insertUser(User user) {
		getSqlMapClientTemplate().insert("User.insertUser", user);
	}
	
	@Override
	public void updatePassword(User user) {
		getSqlMapClientTemplate().insert("User.updateMD5Password", user);
	}
}
