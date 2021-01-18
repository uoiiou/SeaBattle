package com.battleship.code;

import com.battleship.code.client.ClientThread;
import com.battleship.code.server.ServerThread;
import com.battleship.code.shipsinfo.ListOfShip;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private TextField ipconnect;
    @FXML
    private TextField ipcreate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String ip = "127.0.0.1";

        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

             ip = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ipcreate.setText(ip);
        ipconnect.setText("127.0.0.1");
    }

    private void loadWindow(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CreateWindow.fxml"));
            Parent part = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(part);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image("file:src/com/battleship/icons/island-with-palm-trees.png"));
            stage.setResizable(false);
            stage.setTitle("Battle Ship");
            stage.setScene(scene);
            stage.show();

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            System.out.println("MainController - " + e);
            e.printStackTrace();
        }
    }

    private boolean checkIP(String str_ip) {
        List<String> arr = new ArrayList<>();
        int count = 0;
        String numb = "";

        for (int i = 0; i < str_ip.length(); i++) {
            if (str_ip.charAt(i) == '.') {
                ++count;

                if (!numb.equals("")) {
                    arr.add(numb);
                    numb = "";
                }
            } else {
                numb += str_ip.charAt(i);
            }
        }

        if (!numb.equals("")) {
            arr.add(numb);
        }

        return ((count == 3) && (arr.size() == 4));
    }

    @FXML
    void connectClicked(ActionEvent event) {
        ListOfShip.getInstance().setWhois("client");
        new ClientThread(ipconnect.getText()).start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println("MainController - " + e);
            e.printStackTrace();
        }

        if ((ipconnect.getText().length() >= 7) && (checkIP(ipconnect.getText()))) {
            if (ClientThread.getIsConnected()) {
                    loadWindow(event);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Connection problem");
                    alert.setHeaderText("Problem connecting to server");
                    alert.setContentText("Server not created");

                    alert.showAndWait();
                }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection problem");
            alert.setHeaderText("Problem connecting to server");
            alert.setContentText("Incorrect IP-address");

            alert.showAndWait();
        }
    }

    @FXML
    void createClicked(ActionEvent event) {
        ListOfShip.getInstance().setWhois("server");
        new ServerThread().start();

        loadWindow(event);
    }

    @FXML
    void ipconnectClicked() {
        ipconnect.selectAll();
    }

    @FXML
    void ipcreateClicked() {
        ipcreate.selectAll();
    }
}