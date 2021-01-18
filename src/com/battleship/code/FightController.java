package com.battleship.code;

import com.battleship.code.client.ClientThread;
import com.battleship.code.server.ServerThread;
import com.battleship.code.shipsinfo.ListOfShip;
import com.battleship.code.shipsinfo.Point;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FightController implements Initializable {
    private static int[][] matrix = new int[10][10];

    @FXML
    private GridPane enemy_field;
    @FXML
    private GridPane client_field;
    @FXML
    private Label l_wait;
    @FXML
    private Label l_turn_enemy;
    @FXML
    private Label l_turn_client;
    @FXML
    private Label l_win;
    @FXML
    private Label l_lose;
    @FXML
    private Label l_exist_deck;

    private String findColor(int ship_numb) {
        switch (ship_numb) {
            case 1: {
                return "#a0bfe8;";
            }
            case 2: {
                return "#2670cb;";
            }
            case 3: {
                return "#033171;";
            }
            case 4: {
                return "#1a2741;";
            }
        }

        return "";
    }

    private void placeClientShips() {
        int x, y;
        ObservableList<Node> childrens = client_field.getChildren();

        for (int i = 0; i < ListOfShip.getInstance().getShips().size(); i++) {
            for (int j = 0; j < ListOfShip.getInstance().getShips().get(i).getCoordinates().size(); j++) {
                x = ListOfShip.getInstance().getShips().get(i).getCoordinates().get(j).getX();
                y = ListOfShip.getInstance().getShips().get(i).getCoordinates().get(j).getY();

                Button button = (Button) childrens.get(x * 10 + y);
                button.setStyle("-fx-background-color: " + findColor(ListOfShip.getInstance().getShips().get(i).getDecks()));
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (ListOfShip.getInstance().getWhois().equals("server")) {
            ServerThread.setStartCommunicate();
            ServerThread.setFightController(this);
        }
        else {
            ClientThread.setStartCommunicate();
            ClientThread.setFightController(this);
        }

        enemy_field.setDisable(true);
        l_wait.setVisible(true);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button button = new Button();
                button.setPrefHeight(35);
                button.setPrefWidth(35);

                GridPane.setConstraints(button, j, i);
                client_field.getChildren().add(button);
            }
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                matrix[i][j] = 0;
            }
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button button = new Button();
                button.setPrefHeight(35);
                button.setPrefWidth(35);

                GridPane.setConstraints(button, j, i);
                enemy_field.getChildren().add(button);

                button.setOnAction(event -> {
                    int x = GridPane.getRowIndex(button);
                    int y = GridPane.getColumnIndex(button);

                    if (matrix[x][y] == 0) {
                        l_exist_deck.setVisible(false);

                        if (ListOfShip.getInstance().getWhois().equals("server")) {
                            ServerThread.setPoints(x, y);
                        } else {
                            ClientThread.setPoints(x, y);
                        }

                        matrix[x][y] = 1;
                    } else {
                        l_exist_deck.setVisible(true);
                        l_turn_enemy.setVisible(false);
                        l_turn_client.setVisible(false);
                    }
                });
            }
        }

        placeClientShips();
    }

    public void setEnableEnemyField() {
        enemy_field.setDisable(false);
        l_wait.setVisible(false);
        l_turn_client.setVisible(true);
        l_turn_enemy.setVisible(false);
    }

    public void setDisableEnemyField() {
        enemy_field.setDisable(true);
        l_wait.setVisible(false);
        l_turn_enemy.setVisible(true);
        l_turn_client.setVisible(false);
    }

    public void hitEnemy(int x, int y) {
        Button button = (Button) enemy_field.getChildren().get(x * 10 + y);
        button.setStyle("-fx-background-color: maroon;");
    }

    public void no_hitEnemy(int x, int y) {
        Button button = (Button) enemy_field.getChildren().get(x * 10 + y);
        button.setStyle("-fx-background-color: black;");
    }

    public void hitClient(int x, int y) {
        Button button = (Button) client_field.getChildren().get(x * 10 + y);
        button.setStyle("-fx-background-color: maroon;");
    }

    public void no_hitClient(int x, int y) {
        Button button = (Button) client_field.getChildren().get(x * 10 + y);
        button.setStyle("-fx-background-color: black;");
    }

    public void paintDisablePoints(List<Point> disable_points) {
        for (Point disable_point : disable_points) {
            Button button = (Button) enemy_field.getChildren().get(disable_point.getX() * 10 + disable_point.getY());
            button.setDisable(true);
        }
    }

    public void endGame(String status) {
        enemy_field.setDisable(true);
        client_field.setDisable(true);
        l_turn_client.setVisible(false);
        l_turn_enemy.setVisible(false);
        l_wait.setVisible(false);

        if (status.equals("Win"))
            l_win.setVisible(true);
        else
            l_lose.setVisible(true);
    }
}