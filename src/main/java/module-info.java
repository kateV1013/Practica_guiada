module ni.edu.uam.practica {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ni.edu.uam.practica.controllers to javafx.fxml;
    opens ni.edu.uam.practica.models to javafx.base;

    exports ni.edu.uam.practica;
    exports ni.edu.uam.practica.controllers;
    exports ni.edu.uam.practica.dao;
    exports ni.edu.uam.practica.models;
    exports ni.edu.uam.practica.models.utils;
}
