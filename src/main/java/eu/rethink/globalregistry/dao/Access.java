package eu.rethink.globalregistry.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import eu.rethink.globalregistry.configuration.Configuration;
import eu.rethink.globalregistry.model.UsersDataset;

public class Access {

	public ArrayList<UsersDataset> getUsersDataset(Connection con) throws SQLException {

		ArrayList<UsersDataset> UsersDatasetList = new ArrayList<UsersDataset>();
		PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM "+Configuration.getInstance().getTable());
		ResultSet result = preparedStatement.executeQuery();
		try {
			while (result.next()) {
				UsersDataset userDataset = new UsersDataset();
				userDataset.setId(result.getString("id"));
				userDataset.setGuid(result.getString("guid"));
				userDataset.setJwt(result.getString("jwt"));
				UsersDatasetList.add(userDataset);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return UsersDatasetList;

	}

	public void insertUserDataset(Connection con, String guid, String jwt) throws SQLException {

		PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO "+Configuration.getInstance().getTable() + "(guid, jwt) VALUES" + "(?,?)");

		preparedStatement.setString(1, guid);
		preparedStatement.setString(2, jwt);

		preparedStatement.executeUpdate();

		System.out.println("Record is inserted into dataset table!");

	}

	public void updateUserDataset(Connection con, String guid, String jwt) throws SQLException {

		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = con.prepareStatement("UPDATE "+Configuration.getInstance().getTable()+" SET jwt = ? " + " WHERE guid = ?");

		preparedStatement.setString(1, jwt);
		preparedStatement.setString(2, guid);

		preparedStatement.executeUpdate();

		System.out.println("Record is updated  to dataset table!");
	}

	public void deleteUserDataset(Connection con, String guid) throws SQLException {

		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = con.prepareStatement("DELETE "+Configuration.getInstance().getTable()+" WHERE guid = ?");

		preparedStatement.setString(1, guid);

		preparedStatement.executeUpdate();

		System.out.println("Record is deleted!");
	}

	public boolean findUserDataset(Connection con, String guid) throws SQLException {

		// TODO Auto-generated method stub
		PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM "+Configuration.getInstance().getTable()+" WHERE guid = ?");

		preparedStatement.setString(1, guid);

		ResultSet result = preparedStatement.executeQuery();
		try {
			if (result.next()) {
				System.out.println("Record is found!");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Record not found!");
		return false;

	}

}
