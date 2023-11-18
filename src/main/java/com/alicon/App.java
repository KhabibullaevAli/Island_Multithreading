package com.alicon;

import com.alicon.animals.Animal;
import com.alicon.utility.InitialClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Square[][] island = InitialClass.createIsland();
        List<Animal> animals = InitialClass.createAnimals(island);
        InitialClass.spreadAnimalsOnIsland(animals, island);

        for (Animal a : animals) {
            a.start();
        }

//            growGrass(island);
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Exception in main sleep");
            }
            growGrass(island);
//        } while (true);
        } while (printAndGetCount(island) > 50);
        System.out.println("Game over");
    }

    static void growGrass(Square[][] island) {
        for (Square[] squares : island) {
            for (int y = 0; y < island[0].length; y++) {
                Square square = squares[y];
                if (square.getGrassAmount() < 50) {square.setGrassAmount(200);}
                square.setGrassAmount(Math.min(square.getGrassAmount() * 2, Square.grassAmountMax));
            }
        }
    }


    static void printSquare(Square square) {
        List<Animal> animals = square.getAnimals();
        for (Animal a : animals) {
            System.out.print(a);
        }
        System.out.println();
    }

    static int printAndGetCount(Square[][] island) {
        AtomicInteger size = new AtomicInteger(0);
        Properties prop;
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("animals.properties")) {
            prop = new Properties();
            prop.load(input);

            for (Square[] squares : island) {
                for (int y = 0; y < island[0].length; y++) {
                    Square square = squares[y];
                    System.out.print("SQ" + square.getX() + "," + square.getY() + ":[");

                    prop.forEach((k, v) -> {
                        String[] propLine = prop.getProperty((String) k).split(" ");
                        String typeName = propLine[0];
                        int id = Integer.parseInt(propLine[1]);
                        int count;
                        synchronized (square.getAnimals()) {
                            count = (int) square.getAnimals().stream().filter(a -> a.getIdAnimal() == id).count();
                        }
                        size.addAndGet(count);
                        System.out.print(typeName + "=" + count + " ");
                    });

                    System.out.print("grass=" + (int) square.getGrassAmount());
                    System.out.print("] ");

                }
                System.out.println();
            }
        } catch (IOException ex) {
            System.out.println("Error in Main App");
        }
        System.out.println("Quantity=" + size);
        return size.get();
    }
}
