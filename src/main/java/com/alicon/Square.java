package com.alicon;

import com.alicon.animals.Animal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Square {
    public Square(int x, int y) {
        this.x = x;
        this.y = y;
    }

//    public static final int animalAmountMax;
    public static final double grassAmountMax;

    static {
        Properties prop = null;
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("island.properties")) {
            prop = new Properties();
            prop.load(input);
        } catch (IOException e) {
        }
        grassAmountMax = Double.parseDouble(prop.getProperty("grassAmountMax"));
    }

    private double grassAmount;
    private int x;
    private int y;
    //сделать файнал
    private List<Animal> animals=new ArrayList<>();

    public double getGrassAmount() {
        return grassAmount;
    }
    public void setGrassAmount(double grassAmount) {
        this.grassAmount = grassAmount;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public List<Animal> getAnimals() {
        return animals;
    }
    public void setAnimals(List<Animal> animals) {
        this.animals = animals;
    }
}
