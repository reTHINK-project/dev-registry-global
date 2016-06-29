package eu.rethink.globalregistry.dao;

import java.sql.Connection;
import java.util.ArrayList;

import eu.rethink.globalregistry.model.UsersDataset;

public class AccessManager {

	public ArrayList<UsersDataset> getUsersDataset() throws Exception {
		ArrayList<UsersDataset> UsersDatasetList = new ArrayList<UsersDataset>();
		Database db = new Database();
		Connection con = db.getConnection();
		Access access = new Access();
		UsersDatasetList = access.getUsersDataset(con);
		return UsersDatasetList;
	}

	public void insertUserDataset(String guid, String jwt) throws Exception {

		Database db = new Database();
		Connection con = db.getConnection();
		Access access = new Access();
		access.insertUserDataset(con, guid, jwt);

	}

	public void updateUserDataset(String guid, String jwt) throws Exception {

		Database db = new Database();
		Connection con = db.getConnection();
		Access access = new Access();
		access.updateUserDataset(con, guid, jwt);

	}

	public void deleteUserDataset(String guid) throws Exception {

		Database db = new Database();
		Connection con = db.getConnection();
		Access access = new Access();
		access.deleteUserDataset(con, guid);
	}

	public boolean findUserDataset(String guid) throws Exception {

		Database db = new Database();
		Connection con = db.getConnection();
		Access access = new Access();
		boolean result = access.findUserDataset(con, guid);

		return result;
	}

}
