package com.example.mob_operator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;

public class HelloApplication extends Application {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/mob_operator";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "1111";
    private Stage primaryStage;
    private Label userLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("User Registration");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(5);
        grid.setHgap(5);

        // Add UI components
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();
        Label addressLabel = new Label("Address:");
        TextArea addressArea = new TextArea();
        Label contactInfoLabel = new Label("Contact Info:");
        TextField contactInfoField = new TextField();
        Button registerButton = new Button("Register");

        registerButton.setOnAction(e -> {
            registerUser(firstNameField.getText(), lastNameField.getText(),
                    addressArea.getText(), contactInfoField.getText());
            primaryStage.close();
            showMainApplication(firstNameField.getText(), lastNameField.getText());
        });

        grid.add(firstNameLabel, 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(lastNameLabel, 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(addressLabel, 0, 2);
        grid.add(addressArea, 1, 2);
        grid.add(contactInfoLabel, 0, 3);
        grid.add(contactInfoField, 1, 3);
        grid.add(registerButton, 1, 4);

        Scene scene = new Scene(grid, 600, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void registerUser(String firstName, String lastName, String address, String contactInfo) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "INSERT INTO Users (FirstName, LastName, Address, ContactInfo) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, address);
                preparedStatement.setString(4, contactInfo);
                preparedStatement.executeUpdate();
                System.out.println("User registered successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showMainApplication(String firstName, String lastName) {
        Stage mainStage = new Stage();
        mainStage.setTitle("Main Application");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(5);
        grid.setHgap(5);

        userLabel = new Label(firstName + " " + lastName);
        userLabel.setStyle("-fx-font-weight: bold;");
        grid.add(userLabel, 1, 0);

        // Fetch the list of contracts for the user from the database
        ListView<String> contractList = new ListView<>();
        String userContracts = getUserContracts(firstName, lastName);
        String[] contractIds = userContracts.split(",");
        contractList.getItems().addAll(contractIds);

        contractList.setOnMouseClicked(event -> {
            String selectedContractId = contractList.getSelectionModel().getSelectedItem();
            showContractDetails(Integer.parseInt(selectedContractId));
        });

        grid.add(contractList, 1, 1);

        Scene scene = new Scene(grid, 400, 300);
        mainStage.setScene(scene);
        mainStage.show();
    }

    private void showContractDetails(int contractId) {
        Stage contractDetailsStage = new Stage();
        contractDetailsStage.setTitle("Contract Details");
        GridPane contractDetailsGrid = new GridPane();
        contractDetailsGrid.setPadding(new Insets(20, 20, 20, 20));
        contractDetailsGrid.setVgap(5);
        contractDetailsGrid.setHgap(5);

        // Fetch contract details from the database
        String[] contractDetails = getContractDetails(contractId);

        // Display contract details
        Label planLabel = new Label("Tariff Plan:");
        Label planDetailsLabel = new Label(contractDetails[0]); // Assuming contractDetails[0] contains the plan details
        Label simCardLabel = new Label("SIM Card:");
        Label simCardDetailsLabel = new Label(contractDetails[1]); // Assuming contractDetails[1] contains the SIM card details
        Label paymentLabel = new Label("Payment:");
        Label paymentDetailsLabel = new Label(contractDetails[2]); // Assuming contractDetails[2] contains the payment details
        Label dataUsageLabel = new Label("Data Usage:");
        Label dataUsageDetailsLabel = new Label(contractDetails[3]); // Assuming contractDetails[3] contains the data usage details

        contractDetailsGrid.add(planLabel, 0, 0);
        contractDetailsGrid.add(planDetailsLabel, 1, 0);
        contractDetailsGrid.add(simCardLabel, 0, 1);
        contractDetailsGrid.add(simCardDetailsLabel, 1, 1);
        contractDetailsGrid.add(paymentLabel, 0, 2);
        contractDetailsGrid.add(paymentDetailsLabel, 1, 2);
        contractDetailsGrid.add(dataUsageLabel, 0, 3);
        contractDetailsGrid.add(dataUsageDetailsLabel, 1, 3);

        Scene contractDetailsScene = new Scene(contractDetailsGrid, 400, 300);
        contractDetailsStage.setScene(contractDetailsScene);
        contractDetailsStage.show();
    }

    private String[] getContractDetails(int contractId) {
        String[] details = new String[4]; // Array to store plan, sim card, payment, and data usage details

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            // Fetch details from the necessary tables based on the contractId
            String query = "SELECT tp.Name AS PlanName, tp.Services AS PlanServices, sc.PhoneNumber AS SimCardNumber, " +
                    "sc.ActivationStatus AS SimCardStatus, p.Amount AS PaymentAmount, p.PaymentDate, " +
                    "du.DataVolume AS DataUsageVolume " +
                    "FROM SubscriptionContracts sc " +
                    "JOIN Tariff_Plans tp ON sc.PlanID = tp.PlanID " +
                    "LEFT JOIN Payments p ON sc.ContractID = p.ContractID " +
                    "LEFT JOIN DataUsage du ON sc.ContractID = du.ContractID " +
                    "WHERE sc.ContractID = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, contractId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        details[0] = resultSet.getString("PlanName") + ": " + resultSet.getString("PlanServices");
                        details[1] = "SIM Card: " + resultSet.getString("SimCardNumber") + " (" + resultSet.getString("SimCardStatus") + ")";
                        details[2] = "Payment: $" + resultSet.getBigDecimal("PaymentAmount") + " on " + resultSet.getDate("PaymentDate");
                        details[3] = "Data Usage: " + resultSet.getBigDecimal("DataUsageVolume") + " GB";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details;
    }

    private String getUserContracts(String firstName, String lastName) {
        String result = "";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "{? = call GetUserContracts(?)}";
            try (CallableStatement statement = connection.prepareCall(query)) {
                statement.registerOutParameter(1, Types.VARCHAR);
                int userId = getUserId(firstName, lastName);
                statement.setInt(2, userId);
                statement.execute();
                result = statement.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private int getUserId(String firstName, String lastName) {
        int userId = 0;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "SELECT UserID FROM Users WHERE FirstName = ? AND LastName = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, firstName);
                statement.setString(2, lastName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        userId = resultSet.getInt("UserID");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
