package com.battleship.code.server;

import com.battleship.code.CommonOfThreads;
import com.battleship.code.FightController;
import com.battleship.code.shipsinfo.ListOfShip;
import com.battleship.code.shipsinfo.Point;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerThread extends Thread {
    private static boolean start_communicate = false;
    private static int x = -1, y = -1;
    private static boolean end = false;
    private static FightController fight_controller;
    private static Socket socket;
    private static InputStream is;
    private static OutputStream os;

    public ServerThread() {}

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8880);
            socket = serverSocket.accept();
            is = socket.getInputStream();
            os = socket.getOutputStream();

            while (!start_communicate) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("ServerThread - " + e);
                    e.printStackTrace();
                }
            }

            os.write(1);
            if (is.read() == 1) {
                System.out.println("Client connected");

                int rand = (int) (Math.random() * 2);
                os.write(rand);

                if (rand == 1) {
                    fight_controller.setDisableEnemyField();
                    waitAnswer();
                } else {
                    fight_controller.setEnableEnemyField();
                    sendAnswer();
                }
            }
        } catch (IOException e) {
            System.out.println("ServerThread - " + e);
            e.printStackTrace();
        }
    }

    public static void setFightController(FightController controller) {
        ServerThread.fight_controller = controller;
    }

    public static void setStartCommunicate() {
        start_communicate = true;
    }

    private static void closeServer() {
        try {
            is.close();
            os.close();
            socket.close();
            System.out.println("Server closed");
        } catch (IOException e) {
            System.out.println("ServerThread - " + e);
            e.printStackTrace();
        }

    }

    private static void waitAnswer() {
        try {
            int row = is.read();
            int column = is.read();
            int is_hit = CommonOfThreads.isHit(row, column);
            int is_died = CommonOfThreads.isDied(row, column);

            os.write(is_hit);
            os.write(is_died);

            if (is_hit == 1)
                fight_controller.hitClient(row, column);
            else
                fight_controller.no_hitClient(row, column);

            if (is_died == 1)
            {
                int index = CommonOfThreads.getIndex(row, column);
                List<Point> disablePoints = ListOfShip.getInstance().getShips().get(index).getDisablePoints();
                os.write(disablePoints.size());

                for (Point disablePoint : disablePoints) {
                    os.write(disablePoint.getX());
                    os.write(disablePoint.getY());
                }

                if (ListOfShip.getInstance().getShip_count() == 0) {
                    os.write(1);
                    fight_controller.endGame("Lose");
                    end = true;

                    closeServer();
                    return;
                }
                else
                    os.write(0);
            }

            if (!end) {
                if (is_hit == 1) {
                    fight_controller.setDisableEnemyField();
                    waitAnswer();
                } else {
                    fight_controller.setEnableEnemyField();
                    ServerThread.x = ServerThread.y = -1;

                    sendAnswer();
                }
            }

            end = false;
        } catch (IOException e) {
            System.out.println("ServerThread - " + e);
            e.printStackTrace();
        }
    }

    private static void sendAnswer() {
        try {
            while((x == -1) && (y == -1))
                sleep(100);

            os.write(ServerThread.x);
            os.write(ServerThread.y);

            int hit = is.read();
            int kill = is.read();

            if (hit == 1)
                fight_controller.hitEnemy(ServerThread.x, ServerThread.y);
            else
                fight_controller.no_hitEnemy(ServerThread.x, ServerThread.y);

            if (kill == 1)
            {
                int count = is.read();
                List<Point> disablePoints = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    Point point = new Point();
                    point.setX(is.read());
                    point.setY(is.read());
                    disablePoints.add(point);
                }

                fight_controller.paintDisablePoints(disablePoints);

                if (is.read() == 1) {
                    fight_controller.endGame("Win");
                    end = true;

                    closeServer();
                    return;
                }
            }

            if (!end) {
                if (hit == 1) {
                    ServerThread.x = ServerThread.y = -1;
                    sendAnswer();
                } else {
                    fight_controller.setDisableEnemyField();
                    waitAnswer();
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("ServerThread - " + e);
            e.printStackTrace();
        }
    }

    public static void setPoints(int x, int y) {
        ServerThread.x = x;
        ServerThread.y = y;
    }
}