package eu.rethink.globalregistry.model;

public class UsersDataset {

	private String id;
	private String jwt;
	private String guid;

	public UsersDataset() {

	}

	public UsersDataset(String id, String jwt, String guid) {
		super();
		this.id = id;
		this.jwt = jwt;
		this.guid = guid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@Override
	public String toString() {
		return "UsersDataset [ id = " + id + ", jwt = " + jwt + ", guid = " + guid + " ]";
	}
}
