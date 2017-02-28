/* This is the driving engine of the program. It parses the command-line
 * arguments and calls the appropriate methods in the other classes.
 *
 * You should edit this file in three ways:
 * 1) Insert your database username and password in the proper places.
 * 2) Implement the generation of the world by reading the world file.
 * 3) Implement the three functions showPossibleMoves, showPlayerAssets
 *    and showScores.
 */
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*; // JDBC stuff.
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.io.*;  // Reading user input.
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class Game
{
    public class Player
    {
        String playername;
        String personnummer;
        String country;
        private String startingArea;

        public Player (String name, String nr, String cntry, String startingArea) {
            this.playername = name;
            this.personnummer = nr;
            this.country = cntry;
            this.startingArea = startingArea;
        }
    }

    String USERNAME = "USERNAME";
    String PASSWORD = "PASSWORD";

    /* Print command optionssetup.
    * /!\ you don't need to change this function! */
    public void optionssetup() {
        System.out.println();
        System.out.println("Setup-Options:");
        System.out.println("        n[ew player] <player name> <personnummer> <country>");
        System.out.println("        d[one]");
        System.out.println();
    }

    /* Print command options.
    * /!\ you don't need to change this function! */
    public void options() {
        System.out.println("\nOptions:");
        System.out.println("    n[ext moves] [area name] [area country]");
        System.out.println("    l[ist properties] [player number] [player country]");
        System.out.println("    s[cores]");
        System.out.println("    r[efund] <area1 name> <area1 country> [area2 name] [area2 country]");
        System.out.println("    b[uy] [name] <area1 name> <area1 country> [area2 name] [area2 country]");
        System.out.println("    m[ove] <area1 name> <area1 country>");
        System.out.println("    p[layers]");
        System.out.println("    q[uit move]");
        System.out.println("    [...] is optional\n");
    }

    /* Given a town name, country and population, this function
     * should try to insert an area and a town (and possibly also a country)
     * for the given attributes.
     */
    void insertTown(Connection conn, String name, String country, String population) throws SQLException  {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;

        try {

            query = "SELECT COUNT(*) AS matchingCountries FROM Countries WHERE name = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, country);
            resultSet = statement.executeQuery();

            resultSet.next();
            boolean countryExists = resultSet.getInt("matchingCountries") == 1;
            

            if (!countryExists) {
                query = "INSERT INTO countries(name) VALUES(?)";
                statement = conn.prepareStatement(query);
                statement.setString(1, country);
                statement.executeUpdate();
            }


            query = "INSERT INTO areas(country, name, population) VALUES(?, ?, ?)";
            statement = conn.prepareStatement(query);
            statement.setString(1, country);
            statement.setString(2, name);
            statement.setInt(3, Integer.parseInt(population));
            statement.executeUpdate();

            query = "INSERT INTO towns(country, name) VALUES(?, ?)";
            statement = conn.prepareStatement(query);
            statement.setString(1, country);
            statement.setString(2, name);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Given a city name, country and population, this function
     * should try to insert an area and a city (and possibly also a country)
     * for the given attributes.
     * The city visitbonus should be set to 0.
     */
    void insertCity(Connection conn, String name, String country, String population) throws SQLException {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;

        try {
            query = "SELECT COUNT(*) AS matchingCountries FROM Countries WHERE name = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, country);
            resultSet = statement.executeQuery();

            resultSet.next();
            boolean countryExists = resultSet.getInt("matchingCountries") == 1;
            

            if (!countryExists) {
                query = "INSERT INTO countries(name) VALUES(?)";
                statement = conn.prepareStatement(query);
                statement.setString(1, country);
                statement.executeUpdate();
            }

            query = "INSERT INTO areas(country, name, population) VALUES(?, ?, ?)";
            statement = conn.prepareStatement(query);
            statement.setString(1, country);
            statement.setString(2, name);
            statement.setInt(3, Integer.parseInt(population));
            statement.executeUpdate();

            query = "INSERT INTO cities(country, name, visitbonus) VALUES(?, ?, ?)";
            statement = conn.prepareStatement(query);
            statement.setString(1, country);
            statement.setString(2, name);
            statement.setInt(3, 0);
            statement.executeUpdate();

            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Given two areas, this function
     * should try to insert a government owned road with tax 0
     * between these two areas.
     */
    void insertRoad(Connection conn, String area1, String country1, String area2, String country2) throws SQLException {
        String query;
        PreparedStatement statement;

        try {
            query = "INSERT INTO roads(fromcountry, fromarea, tocountry, toarea, ownercountry, ownerpersonnummer, roadtax) VALUES(?, ?, ?, ?, '', '', 0)";
            statement = conn.prepareStatement(query);
            statement.setString(1, country1);
            statement.setString(2, area1);
            statement.setString(3, country2);
            statement.setString(4, area2);
            statement.executeUpdate();          
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Given a player, this function
     * should return the area name of the player's current location.
     */
    String getCurrentArea(Connection conn, Player person) throws SQLException {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;

        try {
            query = "SELECT locationarea FROM Persons WHERE country = ? AND personnummer = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, person.country);
            statement.setString(2, person.personnummer);
            resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getString("locationarea");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    /* Given a player, this function
     * should return the country name of the player's current location.
     */
    String getCurrentCountry(Connection conn, Player person) throws SQLException {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;

        try {
            query = "SELECT locationcountry FROM Persons WHERE country = ? AND personnummer = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, person.country);
            statement.setString(2, person.personnummer);
            resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getString("locationcountry");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    /* Given a player, this function
     * should try to insert a table entry in persons for this player
     * and return 1 in case of a success and 0 otherwise.
     * The location should be random and the budget should be 1000.
     */
    int createPlayer(Connection conn, Player person) throws SQLException {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;

        try {
          query = "SELECT country, name " +
            "FROM areas " +
            "ORDER BY random() " +
            "LIMIT 1 ";
            statement = conn.prepareStatement(query);
            resultSet = statement.executeQuery();
            resultSet.next();
            String country = resultSet.getString("country");
            String area = resultSet.getString("name");

            query = "INSERT INTO persons(country, personnummer, name, locationcountry, locationarea, budget) VALUES(?, ?, ?, ?, ?, 1000)";
            statement = conn.prepareStatement(query);
            statement.setString(1, person.country);
            statement.setString(2, person.personnummer);
            statement.setString(3, person.playername);
            statement.setString(4, country);
            statement.setString(5, area);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /* Given a player and an area name and country name, this function
     * sould show all directly-reachable destinations for the player from the
     * area from the arguments.
     * The output should include area names, country names and the associated road-taxes
     */
    void getNextMoves(Connection conn, Player person, String area, String country) throws SQLException {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            query = "SELECT destcountry, destarea FROM nextmoves WHERE personcountry = ? AND personnummer = ? AND country = ? AND area = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, person.country);
            statement.setString(2, person.personnummer);
            statement.setString(3, country);
            statement.setString(4, area);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                stringBuilder.append(resultSet.getString("destcountry") + ", " + resultSet.getString("destarea") + ", ");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Reachable areas: " + stringBuilder.toString());
    }

    /* Given a player, this function
     * sould show all directly-reachable destinations for the player from
     * the player's current location.
     * The output should include area names, country names and the associated road-taxes
     */
    void getNextMoves(Connection conn, Player person) throws SQLException {
        String country = getCurrentCountry(conn, person);
        String area = getCurrentArea(conn, person);

        getNextMoves(conn, person, country, area);
    }

    /* Given a personnummer and a country, this function
     * should list all properties (roads and hotels) of the person
     * that is identified by the tuple of personnummer and country.
     */
    void listProperties(Connection conn, String personnummer, String country) {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            query = "SELECT name, locationcountry, locationname FROM hotels WHERE ownercountry = ? AND ownerpersonnummer = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, country);
            statement.setString(2, personnummer);
            resultSet = statement.executeQuery();

            System.out.println("HOTELS:\n");
            while (resultSet.next()) {
                stringBuilder.append("\n" + resultSet.getString("name") + ", "
                    + resultSet.getString("locationcountry") + ", "
                    + resultSet.getString("locationname"));
            }

            query = "SELECT fromcountry, fromarea, tocountry, toarea FROM roads WHERE ownercountry = ? AND ownerpersonnummer = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, country);
            statement.setString(2, personnummer);
            resultSet = statement.executeQuery();
            stringBuilder.append("\nROADS:");
            while (resultSet.next()) {
                stringBuilder.append("\nFrom: "
                    + resultSet.getString("fromcountry") + ", "
                    + resultSet.getString("fromarea") + " To: "
                    + resultSet.getString("tocountry") + ", "
                    + resultSet.getString("toarea"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Properties: " + stringBuilder.toString());
    }

    /* Given a player, this function
     * should list all properties of the player.
     */
    void listProperties(Connection conn, Player person) throws SQLException {
        listProperties(conn, person.personnummer, person.country);
    }

    /* This function should print the budget, assets and refund values for all players.
     */
    void showScores(Connection conn) throws SQLException {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            query = "SELECT * FROM assetsummaryy";
            statement = conn.prepareStatement(query);
            resultSet = statement.executeQuery();

            System.out.println("Assetsummary:\nCountry\tPerson number\tBudget\tAssets\tReclaimable");
            while (resultSet.next()) {
                stringBuilder.append("\n" + resultSet.getString("country") + "\t"
                    + resultSet.getString("personnummer") + "\t"
                    + resultSet.getString("budget") + "\t"
                    + resultSet.getString("assets") + "\t"
                    + resultSet.getString("reclaimable"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Scores:\n" + stringBuilder.toString());
    }

    /* Given a player, a from area and a to area, this function
     * should try to sell the road between these areas owned by the player
     * and return 1 in case of a success and 0 otherwise.
     */
    int sellRoad(Connection conn, Player person, String area1, String country1, String area2, String country2) throws SQLException {
        String query;
        PreparedStatement statement;

        try {
            query = "UPDATE persons SET ownercountry = ?, ownerpersonnummer = ? WHERE fromcountry = ? AND fromarea = ? AND tocountry = ? AND toarea = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, person.country);
            statement.setString(2, person.personnummer);
            statement.setString(3, country1);
            statement.setString(4, area1);
            statement.setString(5, country2);
            statement.setString(6, area2);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /* Given a player and a city, this function
     * should try to sell the hotel in this city owned by the player
     * and return 1 in case of a success and 0 otherwise.
     */
    int sellHotel(Connection conn, Player person, String city, String country) throws SQLException {
        String query;
        PreparedStatement statement;

        try {
            query = "DELETE FROM hotels WHERE ownercountry = ?, ownerpersonnummer = ? AND locationcountry = ? AND locationname = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, person.country);
            statement.setString(2, person.personnummer);
            statement.setString(3, country);
            statement.setString(4, city);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /* Given a player, a from area and a to area, this function
     * should try to buy a road between these areas owned by the player
     * and return 1 in case of a success and 0 otherwise.
     */
    int buyRoad(Connection conn, Player person, String area1, String country1, String area2, String country2) throws SQLException {
        String query;
        PreparedStatement statement;

        try {
            query = "INSERT INTO roads(fromcountry, fromarea, tocountry, toarea, ownercountry, ownerpersonnummer) VALUES(?, ?, ?, ?, ?, ?)";
            statement = conn.prepareStatement(query);
            statement.setString(1, country1);
            statement.setString(2, area1);
            statement.setString(3, country2);
            statement.setString(4, area2);
            statement.setString(5, person.country);
            statement.setString(6, person.personnummer);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /* Given a player and a city, this function
     * should try to buy a hotel in this city owned by the player
     * and return 1 in case of a success and 0 otherwise.
     */
    int buyHotel(Connection conn, Player person, String name, String city, String country) throws SQLException {
        String query;
        PreparedStatement statement;

        try {
            query = "INSERT INTO Hotels(name, locationcountry, locationname, ownercountry, ownerpersonnummer) "+
                    "VALUES(?, ?, ?, ?, ?)";
            statement = conn.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, country);
            statement.setString(3, city);
            statement.setString(4, person.country);
            statement.setString(5, person.personnummer);
            return statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /* Given a player and a new location, this function
     * should try to update the players location
     * and return 1 in case of a success and 0 otherwise.
     */
    int changeLocation(Connection conn, Player person, String area, String country) throws SQLException {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;

        int updated = 0;
        try {
            query = "UPDATE Persons "+
                    "SET locationarea = ?, locationcountry = ? "+
                    "WHERE country = ? AND personnummer = ? ";
            statement = conn.prepareStatement(query);
            statement.setString(1, area);
            statement.setString(2, country);
            statement.setString(3, person.country);
            statement.setString(4, person.personnummer);
            updated = statement.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return Math.min(updated, 1);
    }

    /* This function should add the visitbonus of 1000 to a random city
     */
    void setVisitingBonus(Connection conn) throws SQLException {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;

        try {
            query = "UPDATE Cities "+
                    "SET visitbonus = visitbonus + 1000 "+
                    "FROM ( "+
                        "SELECT country, name "+
                        "FROM Cities "+
                        "ORDER BY random() "+
                        "LIMIT 1 "+
                    ") AS random_city "+
                    "WHERE Cities.country = random_city.country "+
                        "AND Cities.name = random_city.name";
            statement = conn.prepareStatement(query);
            statement.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /* This function should print the winner of the game based on the currently highest budget.
     */
    void announceWinner(Connection conn) throws SQLException {
        String query;
        PreparedStatement statement;
        ResultSet resultSet;

        try {
            query = "SELECT personnummer, country "+
                    "FROM Persons "+
                    "WHERE country <> '' AND personnummer <> '' "+
                    "ORDER BY budget DESC ";
            statement = conn.prepareStatement(query);
            resultSet = statement.executeQuery();
            boolean foundWinner = resultSet.next();
            if (foundWinner) {
                System.out.println("And the winner is: "
                    +resultSet.getString("personnummer")
                    +", "+resultSet.getString("country"));
            } else {
                System.out.println("There are no winners :(");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    void play (String worldfile) throws IOException {

        // Read username and password from config.cfg
        try {
            BufferedReader nf = new BufferedReader(new FileReader("config.cfg"));
            String line;
            if ((line = nf.readLine()) != null) {
                USERNAME = line;
            }
            if ((line = nf.readLine()) != null) {
                PASSWORD = line;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (USERNAME.equals("USERNAME") || PASSWORD.equals("PASSWORD")) {
            System.out.println("CONFIG FILE HAS WRONG FORMAT");
            return;
        }

        try {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            String url = "jdbc:postgresql://ate.ita.chalmers.se/";
            Properties props = new Properties();
            props.setProperty("user",USERNAME);
            props.setProperty("password",PASSWORD);

            final Connection conn = DriverManager.getConnection(url, props);

            PreparedStatement clearStatement = conn.prepareStatement(
                "delete from roads cascade; "+
                "delete from hotels cascade; "+
                "delete from persons cascade; "+
                "delete from towns cascade; "+
                "delete from cities cascade; "+
                "delete from areas cascade; "+
                "delete from countries cascade;"
                );
            clearStatement.executeUpdate();

            /* This block creates the government entry and the necessary
             * country and area for that.
             */
            try {
                PreparedStatement statement = conn.prepareStatement("INSERT INTO Countries (name) VALUES (?)");
                statement.setString(1, "");
                statement.executeUpdate();
                statement = conn.prepareStatement("INSERT INTO Areas (country, name, population) VALUES (?, ?, cast(? as INT))");
                statement.setString(1, "");
                statement.setString(2, "");
                statement.setString(3, "1");
                statement.executeUpdate();
                statement = conn.prepareStatement("INSERT INTO Persons (country, personnummer, name, locationcountry, locationarea, budget) VALUES (?, ?, ?, ?, ?, cast(? as NUMERIC))");
                statement.setString(1, "");
                statement.setString(2, "");
                statement.setString(3, "Government");
                statement.setString(4, "");
                statement.setString(5, "");
                statement.setString(6, "0");
                statement.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            // Initialize the database from the worldfile
            try {
                conn.setAutoCommit(false);
                BufferedReader br = new BufferedReader(new FileReader(worldfile));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] cmd = line.split(" +");
                    if ("ROAD".equals(cmd[0]) && (cmd.length == 5)) {
                        insertRoad(conn, cmd[1], cmd[2], cmd[3], cmd[4]);
                    } else if ("TOWN".equals(cmd[0]) && (cmd.length == 4)) {
                        /* Create an area and a town entry in the database */
                        insertTown(conn, cmd[1], cmd[2], cmd[3]);
                    } else if ("CITY".equals(cmd[0]) && (cmd.length == 4)) {
                        /* Create an area and a city entry in the database */
                        insertCity(conn, cmd[1], cmd[2], cmd[3]);
                    }
                }
                conn.commit();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }

            ArrayList<Player> players = new ArrayList<Player>();

            while(true) {
                optionssetup();
                String mode = readLine("? > ");
                String[] cmd = mode.split(" +");
                cmd[0] = cmd[0].toLowerCase();
                if ("new player".startsWith(cmd[0]) && (cmd.length == 5)) {
                    System.out.println("new Player("+cmd[2]+", "+cmd[3]+", "+cmd[4]+")");
                    Player nextplayer = new Player(cmd[2], cmd[3], cmd[4], "");
                    if (createPlayer(conn, nextplayer) == 1) {
                        players.add(nextplayer);
                    }
                } else if ("done".startsWith(cmd[0]) && (cmd.length == 1)) {
                    break;
                } else {
                    System.out.println("\nInvalid option.");
                }
            }

            System.out.println("\nGL HF!");
            int roundcounter = 1;
            int maxrounds = 5;
            while(roundcounter <= maxrounds) {
                System.out.println("\nWe are starting the " + roundcounter + ". round!!!");
                /* for each player from the playerlist */
                for (int i = 0; i < players.size(); ++i) {
                    System.out.println("\nIt's your turn " + players.get(i).playername + "!");
                    System.out.println("You are currently located in " + getCurrentArea(conn, players.get(i)) + " (" + getCurrentCountry(conn, players.get(i)) + ")");
                    while (true) {
                        options();
                        String mode = readLine("? > ");
                        String[] cmd = mode.split(" +");
                        cmd[0] = cmd[0].toLowerCase();
                        if ("next moves".startsWith(cmd[0]) && (cmd.length == 1 || cmd.length == 3)) {
                            /* Show next moves from a location or current location. Turn continues. */
                            if (cmd.length == 1) {
                                String area = getCurrentArea(conn, players.get(i));
                                String country = getCurrentCountry(conn, players.get(i));
                                getNextMoves(conn, players.get(i));
                            } else {
                                getNextMoves(conn, players.get(i), cmd[1], cmd[2]);
                            }
                        } else if ("list properties".startsWith(cmd[0]) && (cmd.length == 1 || cmd.length == 3)) {
                            /* List properties of a player. Can be a specified player
                               or the player himself. Turn continues. */
                            if (cmd.length == 1) {
                                listProperties(conn, players.get(i));
                            } else {
                                listProperties(conn, cmd[1], cmd[2]);
                            }
                        } else if ("scores".startsWith(cmd[0]) && cmd.length == 1) {
                            /* Show scores for all players. Turn continues. */
                            showScores(conn);
                        } else if ("players".startsWith(cmd[0]) && cmd.length == 1) {
                            /* Show scores for all players. Turn continues. */
                            System.out.println("\nPlayers:");
                            for (int k = 0; k < players.size(); ++k) {
                                System.out.println("\t" + players.get(k).playername + ": " + players.get(k).personnummer + " (" + players.get(k).country + ") ");
                            }
                        } else if ("refund".startsWith(cmd[0]) && (cmd.length == 3 || cmd.length == 5)) {
                            if (cmd.length == 5) {
                                /* Sell road from arguments. If no road was sold the turn
                                   continues. Otherwise the turn ends. */
                                if (sellRoad(conn, players.get(i), cmd[1], cmd[2], cmd[3], cmd[4]) == 1) {
                                    break;
                                } else {
                                    System.out.println("\nTry something else.");
                                }
                            } else {
                                /* Sell hotel from arguments. If no hotel was sold the turn
                                   continues. Otherwise the turn ends. */
                                if (sellHotel(conn, players.get(i), cmd[1], cmd[2]) == 1) {
                                    break;
                                } else {
                                    System.out.println("\nTry something else.");
                                }
                            }
                        } else if ("buy".startsWith(cmd[0]) && (cmd.length == 4 || cmd.length == 5)) {
                            if (cmd.length == 5) {
                                /* Buy road from arguments. If no road was bought the turn
                                   continues. Otherwise the turn ends. */
                                if (buyRoad(conn, players.get(i), cmd[1], cmd[2], cmd[3], cmd[4]) == 1) {
                                    break;
                                } else {
                                    System.out.println("\nTry something else.");
                                }
                            } else {
                                /* Buy hotel from arguments. If no hotel was bought the turn
                                   continues. Otherwise the turn ends. */
                                if (buyHotel(conn, players.get(i), cmd[1], cmd[2], cmd[3]) == 1) {
                                    break;
                                } else {
                                    System.out.println("\nTry something else.");
                                }
                            }
                        } else if ("move".startsWith(cmd[0]) && cmd.length == 3) {
                            /* Change the location of the player to the area from the arguments.
                               If the move was legal the turn ends. Otherwise the turn continues. */
                            if (changeLocation(conn, players.get(i), cmd[1], cmd[2]) == 1) {
                                break;
                            } else {
                                System.out.println("\nTry something else.");
                            }
                        } else if ("quit".startsWith(cmd[0]) && cmd.length == 1) {
                            /* End the move of the player without any action */
                            break;
                        } else {
                            System.out.println("\nYou chose an invalid option. Try again.");
                        }
                    }
                }
                setVisitingBonus(conn);
                ++roundcounter;
            }
            announceWinner(conn);
            System.out.println("\nGG!\n");

            conn.close();
        } catch (SQLException e) {
            System.err.println(e);
            System.exit(2);
        }
    }

    private String readLine(String s) throws IOException {
        System.out.print(s);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        char c;
        StringBuilder stringBuilder = new StringBuilder();
        do {
            c = (char) bufferedReader.read();
            stringBuilder.append(c);
        } while(String.valueOf(c).matches(".")); // Without the DOTALL switch, the dot in a java regex matches all characters except newlines

        System.out.println("");
        stringBuilder.deleteCharAt(stringBuilder.length()-1);

        return stringBuilder.toString();
    }

    /* main: parses the input commands.
    * /!\ You don't need to change this function! */
    public static void main(String[] args) throws Exception
    {

        String worldfile = args[0];
        Game g = new Game();

        g.play(worldfile);
    }
}

