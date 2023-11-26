package com.example.mob_operator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import java.sql.*;

public class HelloApplication extends Application {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/mob_operator";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "1111";
    private Stage primaryStage;
    private Label userLabel;
    private ListView<String> userList;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("User Registration");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(5);
        grid.setHgap(5);

        Label firstNameLabel = new Label("Имя:");
        TextField firstNameField = new TextField();
        firstNameField.setPrefWidth(200);
        Label lastNameLabel = new Label("Фамилия:");
        TextField lastNameField = new TextField();
        lastNameField.setPrefWidth(200);
        Label addressLabel = new Label("Адрес:");
        TextArea addressArea = new TextArea();
        addressArea.setPrefWidth(200);
        Label contactInfoLabel = new Label("Информация:");
        TextField contactInfoField = new TextField();
        contactInfoField.setPrefWidth(200);
        Button registerButton = new Button("Зарегистрироваться");
        registerButton.setOnAction(e -> {
            registerUser(firstNameField.getText(), lastNameField.getText(), addressArea.getText(), contactInfoField.getText());
            primaryStage.close();
            showMainApplication(firstNameField.getText(), lastNameField.getText());
        });
        Label userListLabel = new Label("Зарегистрированные пользователи");
        grid.add(userListLabel, 2, 0);
        // Создание ListView для списка пользователей
        userList = new ListView<>();
        userList.setPrefWidth(200);

        // Получение списка пользователей из базы данных
        List<String> users = getUsers();
        // Добавление пользователей в ListView
        userList.getItems().addAll(users);
        // Добавление обработчика события клика на пользователя в списке
        userList.setOnMouseClicked(event -> {
            String selectedUser = userList.getSelectionModel().getSelectedItem();
            // Показать информацию о выбранном пользователе
            showUserInfo(selectedUser);
        });

        // Добавление форм и списка пользователей в GridPane
        grid.add(firstNameLabel, 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(lastNameLabel, 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(addressLabel, 0, 2);
        grid.add(addressArea, 1, 2);
        grid.add(contactInfoLabel, 0, 3);
        grid.add(contactInfoField, 1, 3);
        grid.add(registerButton, 1, 4);
        grid.add(userList, 2, 1, 1, 5);
        Scene scene = new Scene(grid, 550, 300);
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
    private List<String> getUsers() {
        List<String> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "SELECT FirstName, LastName FROM Users";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String firstName = resultSet.getString("FirstName");
                    String lastName = resultSet.getString("LastName");
                    users.add(firstName + " " + lastName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    private void showUserInfo(String selectedUser) {
        if (selectedUser != null) {
            String[] nameParts = selectedUser.split(" ");
            String firstName = nameParts[0];
            String lastName = nameParts[1];
            showMainApplication(firstName, lastName);
        }
    }
    private void showMainApplication(String firstName, String lastName) {
        primaryStage.close(); // Закрыть стартовое окно

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
        List<String> contractIds = getUserContracts(firstName, lastName);

        // Display each contract ID separately in the ListView
        ListView<String> contractList = new ListView<>();
        contractList.getItems().addAll(contractIds);

        contractList.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);

                    // Добавляем обработчик события клика на текст
                    setOnMouseClicked(event -> {
                        String selectedContractId = getItem();
                        showContractDetails(Integer.parseInt(selectedContractId));
                    });
                }
            }
        });

        grid.add(contractList, 1, 1);

        Scene scene = new Scene(grid, 400, 300);

        Button backButton = new Button("К выбору пользователей");
        backButton.setOnAction(e -> {
            mainStage.close(); // Закрыть главный экран
            primaryStage.show(); // Показать стартовое окно
        });
        grid.add(backButton, 1, 5);
        mainStage.setScene(scene);
        mainStage.show();
    }

    private void showContractDetails(int contractId) {
        int planId = getPlanIDByContractID(contractId);
        if (planId == 10) {
            Stage newStage = new Stage();
            newStage.setTitle("Tariff Plans");
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(20, 20, 20, 20));
            grid.setVgap(5);
            grid.setHgap(5);

            // Получение списка тарифных планов из базы данных
            List<String> tariffPlans = getTariffPlans();

            // Создание ListView для списка тарифных планов
            ListView<String> tariffPlanList = new ListView<>();
            tariffPlanList.setPrefWidth(200);
            tariffPlanList.getItems().addAll(tariffPlans);
            tariffPlanList.setOnMouseClicked(event -> {
                String selectedTariffPlan = tariffPlanList.getSelectionModel().getSelectedItem();
                int planId1 = getPlanIdByName(selectedTariffPlan);
                updateContractPlanId(contractId, planId1);
                newStage.close(); // Закрыть окно со списком тарифных планов
                showContractDetails(contractId); // Обновить информацию о контракте
            });
            grid.add(tariffPlanList, 0, 0);
            Scene scene = new Scene(grid, 400, 300);
            newStage.setScene(scene);
            newStage.show();
        }
        else {
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
            Label dataUsageLabel = new Label("Data Usage:");
            Label dataUsageDetailsLabel = new Label(contractDetails[2]); // Assuming contractDetails[3] contains the data usage details

            contractDetailsGrid.add(planLabel, 0, 0);
            contractDetailsGrid.add(planDetailsLabel, 1, 0);
            contractDetailsGrid.add(simCardLabel, 0, 1);
            contractDetailsGrid.add(simCardDetailsLabel, 1, 1);
            contractDetailsGrid.add(dataUsageLabel, 0, 2);
            contractDetailsGrid.add(dataUsageDetailsLabel, 1, 2);

            Scene contractDetailsScene = new Scene(contractDetailsGrid, 600, 300);
            contractDetailsStage.setScene(contractDetailsScene);
            contractDetailsStage.show();
        }
    }
    private int getPlanIdByName(String planName) {
        int planId = 0;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "SELECT PlanID FROM Tariff_Plans WHERE Name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, planName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        planId = resultSet.getInt("PlanID");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return planId;
    }
    private List<String> getTariffPlans() {
        List<String> tariffPlans = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "SELECT Name FROM Tariff_Plans";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String planName = resultSet.getString("Name");
                    tariffPlans.add(planName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tariffPlans;
    }
    private int getPlanIDByContractID(int contractId) {
        int planId = 0;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "{? = call getPlanIDByContractID(?)}";
            try (CallableStatement statement = connection.prepareCall(query)) {
                statement.registerOutParameter(1, Types.VARCHAR);
                statement.setInt(2, contractId);
                statement.execute();
                planId = statement.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return planId;
    }
    private String[] getContractDetails(int contractId) {
        String[] details = new String[3]; // Array to store plan, sim card, payment, and data usage details

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            // Fetch details from the necessary tables based on the contractId
            String query = "SELECT tp.Name AS PlanName, tp.Services AS PlanServices, sim.PhoneNumber AS SimCardNumber, " +
                    "sim.ActivationStatus AS SimCardStatus, p.Amount AS PaymentAmount, p.PaymentDate, " +
                    "du.DataVolume AS DataUsageVolume " +
                    "FROM SubscriptionContracts sc " +
                    "JOIN Tariff_Plans tp ON sc.PlanID = tp.PlanID " +
                    "JOIN SIM_Cards sim ON sim.ContractID = sc.ContractID " +
                    "LEFT JOIN Payments p ON sc.ContractID = p.ContractID " +
                    "LEFT JOIN Data_Usage du ON sc.ContractID = du.ContractID " +
                    "WHERE sc.ContractID = ?";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, contractId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        details[0] = resultSet.getString("PlanName") + ": " + resultSet.getString("PlanServices");
                        details[1] = resultSet.getString("SimCardNumber") + " (" + resultSet.getString("SimCardStatus") + ")";
                        details[2] = resultSet.getBigDecimal("DataUsageVolume") + " GB";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details;
    }
    private void updateContractPlanId(int contractId, int planId) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "UPDATE SubscriptionContracts SET PlanID = ? WHERE ContractID = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, planId);
                statement.setInt(2, contractId);
                statement.executeUpdate();
                System.out.println("Contract plan updated successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private List<String> getUserContracts(String firstName, String lastName) {
        List<String> contractIds = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "{? = call GetUserContracts(?)}";
            try (CallableStatement statement = connection.prepareCall(query)) {
                statement.registerOutParameter(1, Types.VARCHAR);
                int userId = getUserId(firstName, lastName);
                statement.setInt(2, userId);
                statement.execute();
                String result = statement.getString(1);
                String[] ids = result.split(";");
                contractIds.addAll(Arrays.asList(ids));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contractIds;
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
