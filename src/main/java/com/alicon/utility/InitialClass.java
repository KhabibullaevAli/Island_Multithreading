package com.alicon.utility;

import com.alicon.App;
import com.alicon.Square;
import com.alicon.animals.Animal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class InitialClass {
    public static List<Animal> createAnimals(Square[][] island) {
        List<Animal> animals = new ArrayList<>();
        Properties prop;
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("animals.properties")) {
            prop = new Properties();
            prop.load(input);
//0AnimalType, 1Id, 2InitialQuantityOnMap, 3MaxWeight, 4FoodWeightForSatiation, 5MaxQuantityForOneSquare, 6Speed, 7IsPredator, 8
            prop.forEach((k, v) -> {
                String[] propLine = prop.getProperty((String) k).split(" ");
                String className = "com.alicon.animals." + propLine[0];
                int id = Integer.parseInt(propLine[1]);
                int initialQuantity = Integer.parseInt(propLine[2]);
                double maxWeight = Double.parseDouble(propLine[3]);
                double foodWeightForSatiation = Double.parseDouble(propLine[4]);
                double weight =(maxWeight)-foodWeightForSatiation*0.2;
                int maxQuantity = Integer.parseInt(propLine[5]);
                int speed = Integer.parseInt(propLine[6]);
                int maxNumOfChild = Integer.parseInt(propLine[7]);
                boolean isPredator = Boolean.parseBoolean(propLine[8]);
                int[] canEatForId = new int[propLine.length - 9];
                for (int i = 9; i < propLine.length; i++) {
                    canEatForId[i - 9] = Integer.parseInt(propLine[i]);
                }

                for (int n = 0; n < initialQuantity; n++) {
                    Animal animal = null;
                    try {
                        animal = (Animal) Class.forName(className).newInstance();
                    } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                        throw new RuntimeException("Exception in Initial class");
                    }
                    animal.setAllButSquare(island, id, maxWeight, weight, foodWeightForSatiation, maxQuantity, speed, maxNumOfChild, isPredator, canEatForId);
                    animals.add(animal);
                }
            });
        } catch (IOException ex) {
            System.out.println("Error creating animals");
        }
//        prop.forEach((k, v) -> System.out.println("Key : " + k + ", Value : " + v));
        return animals;
    }

    //Creating Island
    public static Square[][] createIsland() {
        Square[][] island = new Square[1][1];
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("island.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            int x = Integer.parseInt(prop.getProperty("width"));
            int y = Integer.parseInt(prop.getProperty("height"));
            island = new Square[x][y];
        } catch (IOException e) {
            System.out.println("Error creating island");
        }

        //Fulfilling squares with random amount of grass and setting x and y
        for (int x = 0; x < island.length; x++) {
            for (int y = 0; y < island[x].length; y++) {
                Square square = new Square(y, x);
                island[x][y] = square;
                square.setGrassAmount(ThreadLocalRandom.current().nextInt(0, (int) Square.grassAmountMax/10)*10);
            }
        }
        return island;
    }

    public static void spreadAnimalsOnIsland(List<Animal> animals, Square[][] island) {
        int maxAttemptAssigning = island.length * island[0].length;
        for (Animal animal : animals) {
            for (int i = 0; i < maxAttemptAssigning; i++) {
                int x = ThreadLocalRandom.current().nextInt(0, island.length);
                int y = ThreadLocalRandom.current().nextInt(0, island[0].length);

                Square square = island[x][y];

                int quantityOfAnimalOnSquare = (int) square.getAnimals().stream().filter(a ->a.getIdAnimal()==animal.getIdAnimal()).count();
                if (quantityOfAnimalOnSquare < animal.getMaxQuantity()) {

                    animal.setSquare(square);
                    square.getAnimals().add(animal);
                    break;
                }
                if (i == maxAttemptAssigning - 1) {
                    throw new RuntimeException("Too much animals for this island");
                }
            }

        }
    }

}
