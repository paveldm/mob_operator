module com.example.mob_operator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.mob_operator to javafx.fxml;
    exports com.example.mob_operator;
}