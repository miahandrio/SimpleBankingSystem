package banking;


import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Objects;


public class DBConnection {
    SQLiteDataSource dataSource;
    public DBConnection(String[] args) {
        String DBUrl = createUrl(args);
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(DBUrl);

        createTable();
    }

    public void insertNewCard(String cardNumber, String pin) {
        String query = "INSERT INTO card (number, pin) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, cardNumber);
            preparedStatement.setString(2, pin);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCard(String cardNumber) {
        String query = "DELETE FROM card WHERE number = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, cardNumber);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean matchCardWithPin(String cardNumber, String pin) {
        String query = "SELECT pin FROM card WHERE number = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, cardNumber);
            return preparedStatement.executeQuery().getString("pin").equals(pin);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getBalance(String cardNumber) {
        String query = "SELECT balance FROM card WHERE number = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, cardNumber);
            return preparedStatement.executeQuery().getInt("balance");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void addBalance(int value, String cardNumber) {
        String query = "UPDATE card " +
                "SET balance = balance + ?" +
                "WHERE number = ?";
        try (Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, value);
            preparedStatement.setString(2, cardNumber);
            preparedStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void doTransfer(int amount, String senderCard, String receiverCard) {
        String withdrawQuery = "UPDATE card " +
                "SET balance = balance - ?" +
                "WHERE number = ?";

        String depositQuery = "UPDATE card " +
                "SET balance = balance + ?" +
                "WHERE number = ?";

        try (Connection connection = dataSource.getConnection();
                PreparedStatement preparedWithdrawStatement = connection.prepareStatement(withdrawQuery);
                PreparedStatement preparedDepositStatement = connection.prepareStatement(depositQuery)) {

                connection.setAutoCommit(false);

                preparedWithdrawStatement.setInt(1, amount);
                preparedWithdrawStatement.setString(2, senderCard);
                preparedWithdrawStatement.execute();

                preparedDepositStatement.setInt(1, amount);
                preparedDepositStatement.setString(2, receiverCard);
                preparedDepositStatement.execute();

                connection.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }


    public boolean isCardExist(String cardNumber) {
        String query = "SELECT * FROM card WHERE number = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, cardNumber);
            ResultSet rs = preparedStatement.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTable() {
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card (" +
                        "id INTEGER PRIMARY KEY," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INTEGER DEFAULT 0)");
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String createUrl(String[] args) {
        String dBName = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (Objects.equals(arg, "-fileName")) {
                dBName = args[i + 1];
                break;
            }
        }
        return "jdbc:sqlite:" + dBName;
    }
}
