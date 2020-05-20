package ru.sbt.bit.oopood.exam.sample1;

import java.sql.*;

public class GameEventProcessor {

    private Connection conn;

    public void initializeDbConnection() throws SQLException {
        conn = DriverManager. getConnection ( "jdbc:mysql://localhost/game_db" , " superuser" ,
                "securepassword" );
    }

    public void processEvent(GameEvent event) throws SQLException {
        if ( conn == null) initializeDbConnection();
        if (event.getType().equals( "build-new-farm") ) {
            BuildFarmEvent buildFarmEvent = (BuildFarmEvent) event;
            Statement stmt = conn .createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT tree, stone, gold FROM object_description WHERE" +
                    "object_type=" + buildFarmEvent.getFarmType());
// Extract data from result set
            if (rs.next()) {
                int tree = rs.getInt( "tree" ); //Retrieve by column name
                int stone = rs.getInt( "age") ;
                int gold = rs.getInt( "gold" );
                Resources availableResources = UserResourcesService.getCurrentResources(event.getUser());
                if ((availableResources.getTree() > tree) && (availableResources.getStone() > stone) &&
                        (availableResources.getGold() > gold)) { // enough resources to build a farm?
                    Farm farm = new Farm(buildFarmEvent.getFarmType());
                    Game.registerNewFarm(farm);
                }
            } else {
                throw new RuntimeException( "Could not find resource information in database for type:" +
                        buildFarmEvent.getFarmType());
            }
            rs.close();
            stmt.close();
            logProcessedEventToDatabase(event);
        } else if (event.getType().equals( "build-new-baracks" )) {
            BuildBaracksEvent buildBaracksEvent = (BuildBaracksEvent) event;
            Statement stmt = conn .createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT gold FROM object_description WHERE object_type="
                    + buildBaracksEvent.getArmyType());
// Extract data from result set
            if (rs.next()) {
                int gold = rs.getInt( "gold" ); //Retrieve by column name
                Resources availableResources = UserResourcesService. getCurrentResources (event.getUser());
                if ((availableResources .getGold() > gold)) { // enough gold to build baracks
                    Barack barack = new Barack(buildBaracksEvent.getArmyType());
                    Game.registerNewBarack(barack);
                }
            } else {
                throw new RuntimeException( "Could not find resource information in database for type:" +
                        buildBaracksEvent);
            }
            rs.close();
            stmt.close();
            logProcessedEventToDatabase(event);
        } else if (event.getType().equals( "build-wall" )) {
// .. some other code here
        }
    }
    private void logProcessedEventToDatabase(GameEvent event) throws SQLException {
        Statement stmt = conn .createStatement();
        stmt.executeQuery( "INSERT INTO events(id, event_type, user) VALUES(" + event.getId() + "," +
                event.getType() + "," + event.getUser().getId() + " )" );
    }
}
