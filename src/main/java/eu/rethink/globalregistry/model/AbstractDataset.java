package eu.rethink.globalregistry.model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Half-Blood on 2/6/2017.
 */
public abstract class AbstractDataset {

    protected String guid;
    protected String salt;
    protected JSONArray userIDs;
    protected String lastUpdate;
    protected String timeout;
    protected String publicKey;
    protected int active;
    protected int revoked;
    protected JSONObject defaults;
    protected int schemaVersion;
    protected JSONArray legacyIDs;


    public String getGUID() {
        return guid;
    }


    public void setGUID(String guid) {
        this.guid = guid;
    }


    public int getSchemaVersion() {
        return schemaVersion;
    }


    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }


    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }


    public JSONArray getUserIDs() {
        return userIDs;
    }

    public JSONObject getUserIDs(int index) {
        return userIDs.getJSONObject(index);
    }


    public void setUserIDs(JSONArray userIDs) {
        this.userIDs = userIDs;
    }


    public String getLastUpdate() {
        return lastUpdate;
    }


    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getTimeout() {
        return timeout;
    }


    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }


    public String getPublicKey() {
        return publicKey;
    }


    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }


    public int getActive() {
        return active;
    }


    public void setActive(int active) {
        this.active = active;
    }

    public int getRevoked() {
        return revoked;
    }

    public void setRevoked(int revoked) {
        this.revoked = revoked;
    }


    public JSONObject getDefaults() {
        return defaults;
    }


    public void setDefaults(JSONObject defaults) {
        this.defaults = defaults;
    }

}
