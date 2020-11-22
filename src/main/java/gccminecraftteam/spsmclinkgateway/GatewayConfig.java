package gccminecraftteam.spsmclinkgateway;

public class GatewayConfig {

    String dbURI;
    String dbDB;

    String jwtsecret;
    String webAppURL;

    String mainServer;
    String limboServer;
    String endServer;

    public GatewayConfig(String dbURI, String dbDB, String jwtsecret, String webAppURL, String mainServer, String limboServer, String endServer) {
        this.dbURI = dbURI;
        this.dbDB = dbDB;
        this.jwtsecret = jwtsecret;
        this.webAppURL = webAppURL;
        this.mainServer = mainServer;
        this.limboServer = limboServer;
        this.endServer = endServer;
    }

    // Jackson needs this empty constructor (do not remove)
    public GatewayConfig() {}

    public String getDbDB() {
        return dbDB;
    }

    public String getDbURI() {
        return dbURI;
    }

    public String getjwtsecret() { return jwtsecret; }

    public String getWebAppURL() { return webAppURL; }

    public String getMainServer() {
        return mainServer;
    }

    public String getLimboServer() {
        return limboServer;
    }

    public String getEndServer() {
        return endServer;
    }

    @Override
    public String toString() {
        return "dbURI: " + dbURI +
                "\ndbDB: " + dbDB +
                "\njwtsecret: " + jwtsecret +
                "\nwebAppURL: " + webAppURL +
                "\nmainServer: " + mainServer +
                "\nlimboServer: " + limboServer +
                "\nendServer: " + endServer
                ;
    }
}
