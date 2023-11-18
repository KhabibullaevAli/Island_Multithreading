package com.alicon.animals;

import com.alicon.Square;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Animal extends Thread {
    private Square[][] island;
    private int idAnimal;
    private double maxWeight;
    private double weight;
    private double foodWeightForSatiation;
    private int maxQuantity;
    private int speed;
    private int maxNumOfChild;
    private boolean isPredator;
    private int[] canEatForId;
    private Square square;

    @Override
    public void run() {
        while (true){

            eatOrDie();

            if (isInterrupted()) break;
            reproduce();

            if (isInterrupted()) break;
            move(island);


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            if (isInterrupted()) break;
        }
    }

    public void setAllButSquare(Square[][] island, int id, double maxWeight, double weight, double foodWeightForSatiation, int maxQuantity, int speed, int maxNumOfChild, boolean isPredator, int[] canEatForId) {
        this.island = island;
        this.idAnimal = id;
        this.maxWeight = maxWeight;
        this.weight = weight;
        this.foodWeightForSatiation = foodWeightForSatiation;
        this.maxQuantity = maxQuantity;
        this.speed = speed;
        this.maxNumOfChild = maxNumOfChild;
        this.isPredator = isPredator;
        this.canEatForId = canEatForId;
    }
    public int getIdAnimal() {return idAnimal;}
    public void setIdAnimal(int idAnimal) {this.idAnimal = idAnimal;}
    public double getMaxWeight() {return maxWeight;}
    public void setMaxWeight(double maxWeight) {this.maxWeight = maxWeight;}
    public double getWeight() {return weight;}
    public void setWeight(double weight) {this.weight = weight;}
    public double getFoodWeightForSatiation() {return foodWeightForSatiation;}
    public void setFoodWeightForSatiation(double foodWeightForSatiation) {this.foodWeightForSatiation = foodWeightForSatiation;}
    public int getMaxQuantity() {return maxQuantity;}
    public void setMaxQuantity(int maxQuantity) {this.maxQuantity = maxQuantity;}
    public int getSpeed() {return speed;}
    public void setSpeed(int speed) {this.speed = speed;}
    public int getMaxNumOfChild() {return maxNumOfChild;}
    public void setMaxNumOfChild(int maxNumOfChild) {this.maxNumOfChild = maxNumOfChild;}
    public boolean isPredator() {return isPredator;}
    public void setPredator(boolean predator) {isPredator = predator;}
    public int[] getCanEatForId() {return canEatForId;}
    public void setCanEatForId(int[] canEatForId) {this.canEatForId = canEatForId;}
    public Square getSquare() {return square;}
    public void setSquare(Square square) {this.square = square;}


    public void move(Square[][] island) {
        //Если голоден или квадрат перенаселен или нет других этого же типа на квадрате, то вероятность движения повышается на 25% за каждый, исходная вероятность - 25%
        int probabilityForMove = 1;
        if (maxWeight - weight > foodWeightForSatiation / 2) probabilityForMove++;
        int quantityOfType;
        synchronized (square.getAnimals()) {
            quantityOfType = (int) square.getAnimals().stream().filter(a -> a.getIdAnimal() == this.getIdAnimal()).count();
        }
        if ((double) quantityOfType / maxQuantity > 0.8) probabilityForMove++;
        if ((double) quantityOfType < 2) probabilityForMove++;
        boolean iWantToMove = ThreadLocalRandom.current().nextInt(1, 5) <= probabilityForMove;

        if (iWantToMove) {
            int times;
            if (speed == 1) times = 1;
            else times = ThreadLocalRandom.current().nextInt(1, speed + 1);
            for (int i = 0; i < times; i++) {
                int direction = ThreadLocalRandom.current().nextInt(1, 5);
                int x = square.getX();
                int y = square.getY();
                //выбираем направление движения
                switch (direction) {
                    case 1:
                        y--;
                        break;
                    case 2:
                        x++;
                        break;
                    case 3:
                        y++;
                        break;
                    case 4:
                        x--;
                        break;
                }
                if (x < 0 || x >= island.length || y < 0 || y >= island[0].length) continue;
                Square newSquare = island[x][y];
                //Если целевая клетка не перенаселена, то двигаемся
                int quantityOfTypeOnNewSquare;
                    synchronized (newSquare.getAnimals()) {
                        quantityOfTypeOnNewSquare = (int) newSquare.getAnimals().stream().filter(a -> a.getIdAnimal() == idAnimal).count();
                    }

                        if (quantityOfTypeOnNewSquare < this.getMaxQuantity()) {
                            synchronized (square.getAnimals()) {
                                square.getAnimals().remove(this);
                            }
                            synchronized (newSquare.getAnimals()) {
                                newSquare.getAnimals().add(this);
                            }
                            this.setSquare(newSquare);
                        }
            }
        }
    }

    public void eatOrDie() {
            // Уменьшаем вес при каждом шаге
            this.setWeight(this.getWeight() - this.getFoodWeightForSatiation() / 3);

            //Ищем кого бы поесть
        synchronized (square.getAnimals()) {
            for (int k = 0; k < 3; k++) {
            List<Animal> allAnimals = square.getAnimals();
            Animal animalToEat = allAnimals.stream().filter(a -> this.canEatForId[a.getIdAnimal()] > 0).findAny().orElse(null);
            if (animalToEat != null) {
                int chance = ThreadLocalRandom.current().nextInt(1, 100);
                if (chance >= this.getCanEatForId()[animalToEat.getIdAnimal()]) {
//                System.out.println(this.toString()+"--Wow i eat--"+animalToEat+"; Sq "+square.getX()+"."+square.getY());
                    this.weight += animalToEat.getWeight();
                    if (this.weight > this.maxWeight) this.weight = this.maxWeight;
                    square.getAnimals().remove(animalToEat);
                    animalToEat.interrupt();
                }

            } else {
                if (!this.isPredator) {
                    if (k>0) break;
                    double oneLoadGrassAmount = this.foodWeightForSatiation / 3;
                        if (square.getGrassAmount() >= oneLoadGrassAmount) {
                            this.weight += oneLoadGrassAmount;
//                        System.out.print(this+"--Wow i eat-- GRASS; GrOld="+square.getGrassAmount());
                            square.setGrassAmount(square.getGrassAmount() - oneLoadGrassAmount);
//                        System.out.println("; GrNew="+square.getGrassAmount());
                        }
                    if (this.weight > this.maxWeight) this.weight = this.maxWeight;
                }
            }
                if (maxWeight - weight > foodWeightForSatiation / 2) break;
        }
                //Если слишком истощен, то умирает
            if (this.getWeight() < this.getMaxWeight() - foodWeightForSatiation - foodWeightForSatiation/5) {
//                System.out.println("--oh i died--"+this+" Sq"+square.getX()+"."+square.getY());
                square.getAnimals().remove(this);
                this.interrupt();
            }
        }
    }

    public void reproduce() {

        synchronized (square.getAnimals()) {
            if (weight < maxWeight - foodWeightForSatiation * 0.8) return ;
            if (ThreadLocalRandom.current().nextInt(0, 5) > 1) return ;
            int quantityOfType = (int) square.getAnimals().stream().filter(a -> a.getIdAnimal() == this.getIdAnimal()).count();
            int availableSlots = this.getMaxQuantity() - quantityOfType;
            if (quantityOfType < 2 || availableSlots < 1) return ;
            int quantityOfChild = ThreadLocalRandom.current().nextInt(0, Math.min(availableSlots, this.getMaxNumOfChild()) + 1);
//            System.out.println(this+" gave birth to "+quantityOfChild);
            for (int i = 0; i < quantityOfChild; i++) {
                Animal animal;
                try {
                    animal = (Animal) Class.forName(this.getClass().getName()).newInstance();
                } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                    throw new RuntimeException("Exception in Reproducing in Animal class");
                }
                animal.setAllButSquare(island, idAnimal, maxWeight, weight, foodWeightForSatiation, maxQuantity, speed, maxNumOfChild, isPredator, canEatForId);
                square.getAnimals().add(animal);
                animal.setSquare(square);
                animal.start();

            }
        }

    }
}
