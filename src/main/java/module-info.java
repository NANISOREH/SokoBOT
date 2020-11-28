module sample {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.logging;
    requires java.base;
    requires com.google.gson;

    exports gui;
    opens gui;
}
