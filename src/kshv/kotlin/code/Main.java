package kshv.kotlin.code;

import javafx.util.Pair;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Stack;

public class Main {

    public static void main(String[] args) {
        Maze maze = new Maze();
        maze.regen(49);
        maze.show(true);
    }
}

class Maze{
    JFrame frame;
    static JPanel panel;
    static int stackSizeMax;
    Rectangle bounds;
    int[][] mazeCells;
    int cellSize = 16;
    Stack<Pair<Integer,Integer>> stack;
    Random rnd;
    int[] pathChooser = new int[]{1,2,3,4};
    ArrayList<Pair<Integer,Integer>> visitedCells;
    ArrayList<Integer> acceptedDirections;
    Thread iterRun;

    public Maze(){
        frame = new JFrame("maze");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        panel = new JPanel(null){
            @Override
            public void paint(Graphics g) {
                for(int x=0;x<mazeCells.length;x++){
                    for (int y=0;y<mazeCells.length;y++){
                        switch (mazeCells[x][y]){
                            case 1: g.setColor(Color.BLACK); break;
                            case 2: g.setColor(Color.WHITE); break;
                        }
                        g.fillRect((cellSize*x)+cellSize,(cellSize*y)+cellSize,cellSize,cellSize);
                    }
                }
            }
        };
        bounds = new Rectangle(400,100,832,848);
        frame.setBounds(bounds);
        panel.setBounds(bounds);
        frame.add(panel);
    }

    void saveToFile(){
        BufferedWriter br = null;
        try {
            br = new BufferedWriter(new FileWriter("assets/output.csv"));
            StringBuilder sb = new StringBuilder();

            int lastElement = 0;

            for (int[] row : mazeCells) {
                for(int element : row) {
                    lastElement++;
                    sb.append(element);

                    if (lastElement!=(row.length*row.length)) {
                        sb.append(",");
                    }
                }
                sb.append("\n");
            }

            br.write(sb.toString());
            br.close();
            System.out.println("output saved");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void show(boolean state){

        regen(mazeCells.length);

        if (state) {
            iterRun = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("maze gen start on " + new Date());
                    int iter = 0;
                    while (!stack.isEmpty()){
                        try {
                            Thread.sleep(1);
                            update();
                            iter++;
                            panel.repaint();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("max gen stack size: "+stackSizeMax);
                    System.out.println("maze gen finish by "+ iter +" iterations on " + new Date());
                    saveToFile();
                }
            });
            iterRun.setDaemon(true);
        }
        frame.setVisible(state);
        iterRun.run();
    }

    private void update() {
        int x_delta,y_delta;
        int direction = pathChooser[rnd.nextInt(pathChooser.length)];
        switch (direction){
             case 1:  x_delta=0;   y_delta=2;  break;
             case 2:  x_delta=2;   y_delta=0;  break;
             case 3:  x_delta=0;  y_delta=-2;  break;
             case 4: x_delta=-2;   y_delta=0;  break;
            default:  x_delta=0;   y_delta=0;  break;
        }
        int current_x = stack.peek().getKey();
        int current_y = stack.peek().getValue();
        System.out.print("current stack size: "+stack.size()+"\r");
        if (stackSizeMax < stack.size()){
            stackSizeMax = stack.size();
        }

        if (!acceptedDirections.contains(direction)){
            acceptedDirections.add(direction);

        }
        if (acceptedDirections.size() >=4){
            stack.pop();
            acceptedDirections.clear();
        }


        if (current_x+x_delta>=1
                && current_y+y_delta>=1
                && current_x+x_delta<=mazeCells.length-2
                && current_y+y_delta<=mazeCells.length-2) {

            if (mazeCells[current_x + x_delta][current_y + y_delta] == 2
                    && !visitedCells.contains(new Pair<>(current_x + x_delta, current_y + y_delta))
                    && !visitedCells.contains(new Pair<>(current_x + x_delta/2, current_y + y_delta/2))) {

                mazeCells[current_x + x_delta/2][current_y + y_delta/2] = 2;


                visitedCells.add(new Pair<>(current_x + x_delta/2, current_y + y_delta/2));
                visitedCells.add(new Pair<>(current_x + x_delta, current_y + y_delta));

                stack.push(new Pair<>(current_x + x_delta/2, current_y + y_delta/2));
                stack.push(new Pair<>(current_x + x_delta, current_y + y_delta));
                acceptedDirections.clear();
            }
        }
    }

    public void regen(int mazeSize) {
        rnd = new Random();
        visitedCells = new ArrayList<>();
        acceptedDirections = new ArrayList<>();
        mazeCells = new int[mazeSize][mazeSize];

        for(int x=0;x<mazeCells.length;x++){
            for (int y=0;y<mazeCells.length;y++){
                if (x%2==1 && y%2==1){
                    mazeCells[x][y]=2;
                }else{
                    mazeCells[x][y]=1;
                }
            }
        }

        stack = new Stack<>();
        //mazeCells[mazeCells.length/2+1][mazeCells.length/2+1] = 2;
        stack.push(new Pair<>(mazeCells.length/2+1,mazeCells.length/2+1));
        visitedCells.add(stack.peek());

    }
}
