package com.qy.sp.fee.dao.impl.mongodb;

import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.DB;
import com.mongodb.Mongo;

public class AuthMongoDbFactory extends SimpleMongoDbFactory{
	private String authDataBaseName;
	private String username;
	private String password;
	public AuthMongoDbFactory(Mongo mongo, String databaseName,String authDataBaseName,String username ,String password) {
		super(mongo, databaseName);
		this.authDataBaseName = authDataBaseName;
		this.username = username;
		this.password = password;
	}
	@Override
	public DB getDb() throws DataAccessException {
		DB db = getDb(authDataBaseName);
		db.authenticate(username, password.toCharArray());
		return super.getDb();
	}
}
