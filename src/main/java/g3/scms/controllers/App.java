package g3.scms.controllers;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.kordamp.bootstrapfx.BootstrapFX;

import g3.scms.api.RestApi;
import g3.scms.model.Request;
import g3.scms.utils.Views;

/**
 * Author: Group 3
 */

public class App extends Application {
    private static Scene scene;

    public static void main(String[] args) {
        try {
            launch();
        } catch (Exception e) { System.out.println(e.getMessage()); }
    }

    @Override
    public void start(Stage stage) throws IOException {
        AnchorPane homePane = (AnchorPane) Views.loadFXML("/views/landing_page");

        // Check if the user has saved login token
        // If yes we don't wanna show the login page else we proceed to the login page

        File file = new File("aastu_scms/src/main/resources/auth.bat");
        if (file.exists()) {
            // check if the token is not tampered with by sending the login request using the saved token
            try {
                boolean isValidSession = App.checkSessionValidity(file);
                if (isValidSession) {
                    // Load the main functionality page
                    AnchorPane signUpPane = (AnchorPane) Views.loadFXML("/views/signup_page");
                    AnchorPane inputField = (AnchorPane) homePane.getChildren()
                            .filtered(node -> node.getId() != null && node.getId().equals("inputFieldAnchorPane"))
                            .get(0);
                    Views.paintPage(signUpPane, inputField, 0, 0, 0, 0);
                } else {
                    // Remove the current invalid token file
                    file.delete();
                }
            } catch (Error e) { } 
        }

        scene = new Scene(homePane, 1080, 540);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.setTitle("Student Clearance");
        stage.getIcons().add(new Image(App.class.getResource("/image/aastu-logo.jpg").toString()));

        stage.show();
    }

    private static boolean checkSessionValidity(File sessionFile) throws IOException {
        // Read the session token
        FileInputStream input = new FileInputStream(sessionFile);
        String token = "";
        var temp = 0;
        while ((temp = input.read()) != -1) token += (char)temp;        
        input.close();

        // Build the request adding the authorization header
        Request request = new Request();
        request.setBaseUrl("http://localhost:1492/");
        request.setPath("api/auth/login");
        request.setHeaderMap("Authorization", token);
        
        // Send get request to the api
        RestApi api = new RestApi();
        var response = api.get(request, (err, res) -> {
            if (err != null) return null;
            return res;
        });
        
        if(response == null) throw new Error("The server is down!");
        if(response.statusCode() == 200) return true;
        if (response.statusCode() == 401) return false;
        return false;
    }
}