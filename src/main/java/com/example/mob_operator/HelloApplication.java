package com.example.mob_operator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.util.*;
import java.sql.*;
public class HelloApplication extends Application {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/mob_operator";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "1111";
    private Stage primaryStage;
    private Label userLabel;
    private ListView<String> userList;
    private Label dataUsageDetailsLabel;
    private Button stopDataUsageButton;
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Регистрация пользователя");
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
            List<String> updatedUsers = getUsers();
            userList.getItems().clear();
            userList.getItems().addAll(updatedUsers);
            primaryStage.close();
            showMainApplication(firstNameField.getText(), lastNameField.getText());
        });
        Label userListLabel = new Label("Зарегистрированные пользователи");
        grid.add(userListLabel, 2, 0);
        userList = new ListView<>();
        userList.setPrefWidth(200);
        List<String> users = getUsers();
        userList.getItems().addAll(users);
        userList.setOnMouseClicked(event -> {
            String selectedUser = userList.getSelectionModel().getSelectedItem();
            showUserInfo(selectedUser);
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
        primaryStage.close();
        Stage mainStage = new Stage();
        mainStage.setTitle("Main Application");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(5);
        grid.setHgap(5);
        userLabel = new Label(firstName + " " + lastName);
        userLabel.setStyle("-fx-font-weight: bold;");
        grid.add(userLabel, 1, 0);
        List<String> contractIds = getUserContracts(firstName, lastName);
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
            mainStage.close();
            primaryStage.show();
        });
        Button deleteButton = new Button("Удалить пользователя");
        deleteButton.setOnAction(e -> {
            String selectedUser = userList.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                String[] nameParts = selectedUser.split(" ");
                String firstNameToDelete = nameParts[0];
                String lastNameToDelete = nameParts[1];
                deleteUser(firstNameToDelete, lastNameToDelete);
                List updatedUsers = getUsers();
                userList.getItems().clear();
                userList.getItems().addAll(updatedUsers);
            }
        });
        grid.add(deleteButton, 1, 4);
        grid.add(backButton, 1, 5);
        mainStage.setScene(scene);
        mainStage.show();
    }
    private void deleteUser(String firstName, String lastName) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            int userId = getUserId(firstName, lastName);
            String deleteDataUsageQuery = "DELETE FROM Data_Usage WHERE ContractID IN " +
                    "(SELECT ContractID FROM SubscriptionContracts WHERE UserID = ?)";
            try (PreparedStatement deleteDataUsageStatement = connection.prepareStatement(deleteDataUsageQuery)) {
                deleteDataUsageStatement.setInt(1, userId);
                deleteDataUsageStatement.executeUpdate();
            }
            String deletePaymentsQuery = "DELETE FROM Payments WHERE ContractID IN " +
                    "(SELECT ContractID FROM SubscriptionContracts WHERE UserID = ?)";
            try (PreparedStatement deletePaymentsStatement = connection.prepareStatement(deletePaymentsQuery)) {
                deletePaymentsStatement.setInt(1, userId);
                deletePaymentsStatement.executeUpdate();
            }
            String deleteSimCardsQuery = "DELETE FROM SIM_Cards WHERE ContractID IN " +
                    "(SELECT ContractID FROM SubscriptionContracts WHERE UserID = ?)";
            try (PreparedStatement deleteSimCardsStatement = connection.prepareStatement(deleteSimCardsQuery)) {
                deleteSimCardsStatement.setInt(1, userId);
                deleteSimCardsStatement.executeUpdate();
            }
            String deleteContractsQuery = "DELETE FROM SubscriptionContracts WHERE UserID = ?";
            try (PreparedStatement deleteContractsStatement = connection.prepareStatement(deleteContractsQuery)) {
                deleteContractsStatement.setInt(1, userId);
                deleteContractsStatement.executeUpdate();
            }
            String deleteUserQuery = "DELETE FROM Users WHERE UserID = ?";
            try (PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery)) {
                deleteUserStatement.setInt(1, userId);
                deleteUserStatement.executeUpdate();
                System.out.println("Пользователь успешно удален");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private Timer dataUsageTimer;
    private double dataVolumeValue = 0.0;
    private void showContractDetails(int contractId) {
        int planId = getPlanIDByContractID(contractId);
        if (planId == 10) {
            Stage newStage = new Stage();
            newStage.setTitle("Тарифные планы");
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(20, 20, 20, 20));
            grid.setVgap(5);
            grid.setHgap(5);
            List<String> tariffPlans = getTariffPlans();
            ListView<String> tariffPlanList = new ListView<>();
            tariffPlanList.setPrefWidth(200);
            tariffPlanList.getItems().addAll(tariffPlans);
            tariffPlanList.setOnMouseClicked(event -> {
                String selectedTariffPlan = tariffPlanList.getSelectionModel().getSelectedItem();
                int planId1 = getPlanIdByName(selectedTariffPlan);
                updateContractPlanId(contractId, planId1);
                newStage.close();
                showContractDetails(contractId);
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
            Label planLabel = new Label("Тарифный план:");
            Label planDetailsLabel = new Label(contractDetails[0]);
            Label simCardLabel = new Label("Сим-карта:");
            Label simCardDetailsLabel = new Label(contractDetails[1]);
            Label dataUsageLabel = new Label("Использование данных:");
            Label dataUsageDetailsLabel = new Label(contractDetails[2]);
            Button payButton = new Button("Оплатить тариф");
            payButton.setOnAction(e -> {
                payTariff(contractId);
            });
            Button startDataUsageButton = new Button("Начать использование данных");
            startDataUsageButton.setOnAction(e -> startDataUsageTimer(contractId));
            contractDetailsGrid.add(startDataUsageButton, 0, 4);
            stopDataUsageButton = new Button("Стоп");
            stopDataUsageButton.setOnAction(e -> stopDataUsageTimer());
            contractDetailsGrid.add(stopDataUsageButton, 1, 4);
            contractDetailsGrid.add(payButton, 0, 3);
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
    private void startDataUsageTimer(int contractId) {
        if (dataUsageTimer == null) {
            dataUsageTimer = new Timer(true);
            dataUsageTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateDataUsage(contractId);
                }
            }, 0, 1000);
        }
        dataUsageDetailsLabel = new Label();
    }
    private void stopDataUsageTimer() {
        if (dataUsageTimer != null) {
            dataUsageTimer.cancel();
            dataUsageTimer.purge();
            dataUsageTimer = null;
        }
    }
    private void updateDataUsage(int contractId) {
        dataVolumeValue += Math.random() * 9.99 + 1.01;
        Platform.runLater(() -> {
            dataUsageDetailsLabel.setText(String.format("%.2f МБ", dataVolumeValue));
        });
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String updateQuery = "UPDATE Data_Usage SET DataVolume = ? WHERE ContractID = ?";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setDouble(1, dataVolumeValue);
                updateStatement.setInt(2, contractId);
                updateStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void payTariff(int contractId) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String simCardStatus = getSimCardStatus(contractId);
            if (simCardStatus.equals("not_activated")) {
                String updateQuery = "UPDATE SIM_Cards SET ActivationStatus = 'activated' WHERE ContractID = ?";
                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                    updateStatement.setInt(1, contractId);
                    updateStatement.executeUpdate();
                    System.out.println("Тариф оплачен успешно!");
                    showContractDetails(contractId);
                }
            } else {
                System.out.println("Сим-карта уже активирована!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private String getSimCardStatus(int contractId) {
        String simCardStatus = "";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
            String query = "SELECT ActivationStatus FROM SIM_Cards WHERE ContractID = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, contractId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        simCardStatus = resultSet.getString("ActivationStatus");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return simCardStatus;
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
        String[] details = new String[3];
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)) {
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
                        details[2] = resultSet.getBigDecimal("DataUsageVolume") + " МБ";
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
                System.out.println("Тарифный план обновлен");
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
