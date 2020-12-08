module sample {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.logging;
    requires java.base;
    requires com.google.gson;

    exports gui;
    opens gui;
    exports game;
    opens game;
    exports solver;
    opens solver;
}
