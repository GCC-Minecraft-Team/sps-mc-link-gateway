package gccminecraftteam.spsmclinkgateway;

public class GatewayConfig {

    String dbURI;
    String dbDB;

    public GatewayConfig(String dbURI, String dbDB) {
        this.dbURI = dbURI;
        this.dbDB = dbDB;
    }

    // Jackson needs this empty constructor (do not remove)
    public GatewayConfig() {}

    public String getDbDB() {
        return dbDB;
    }

    public String getDbURI() {
        return dbURI;
    }

    @Override
    public String toString() {
        return "dbURI: " + dbURI + "\ndbDB: " + dbDB;
    }
}
