module sample {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    //requires java.logging;
    requires com.google.gson;

    exports gui;
    opens gui;
}
