package com.magmaguy.freeminecraftmodels.dataconverter;

import com.magmaguy.freeminecraftmodels.Daedalus;
import com.magmaguy.freeminecraftmodels.utils.MathToolkit;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HitboxBlueprint {
    private final String modelName;
    private Vector modelOffset = new Vector();
    private BoneBlueprint parent = null;
    private double widthX;
    private double widthZ;
    private double height;

    public HitboxBlueprint(Map<String, Object> boneJSON, HashMap<String, Object> values, String modelName, BoneBlueprint parent) {
        this.parent = parent;
        this.modelName = modelName;
        ArrayList<?> childrenValues = (ArrayList<?>) boneJSON.get("children");
        if (childrenValues.size() > 1) {
            Daedalus.log("Model " + modelName + " has more than one value defining a hitbox! Only the first cube will be used to define the hitbox.");
        }
        if (childrenValues.isEmpty()) {
            Daedalus.log("Model " + modelName + " has a hitbox bone but no hitbox cube! This means the hitbox won't be able to generate correctly!");
            return;
        }
        if (childrenValues.get(0) instanceof String) {
            parseCube((Map<String, Object>) values.get(childrenValues.get(0)));
        } else {
            Daedalus.log("Model " + modelName + " has an invalid hitbox! The hitbox bone should only have one cube in it defining its boundaries.");
        }

    }

    public Vector getModelOffset() {
        return modelOffset.clone();
    }

    private void parseCube(Map<String, Object> cubeJSON) {
        double scaleFactor = .16D / 2.5;
        ArrayList<Double> fromList = (ArrayList<Double>) cubeJSON.get("from");
        Vector from = new Vector(MathToolkit.roundFourDecimalPlaces(fromList.get(0) * scaleFactor), MathToolkit.roundFourDecimalPlaces(fromList.get(1) * scaleFactor), MathToolkit.roundFourDecimalPlaces(fromList.get(2) * scaleFactor));
        ArrayList<Double> toList = (ArrayList<Double>) cubeJSON.get("to");
        Vector to = new Vector(MathToolkit.roundFourDecimalPlaces(toList.get(0) * scaleFactor), MathToolkit.roundFourDecimalPlaces(toList.get(1) * scaleFactor),MathToolkit.roundFourDecimalPlaces(toList.get(2) * scaleFactor));
        widthX = Math.abs(to.getX() - from.getX());
        widthZ = Math.abs(to.getZ() - from.getZ());
        height = Math.abs(from.getY() - to.getY());
        modelOffset = new Vector(0, 0, 0);
    }

    public String getModelName() {
        return modelName;
    }

    public BoneBlueprint getParent() {
        return parent;
    }

    public double getWidthX() {
        return widthX;
    }

    public double getWidthZ() {
        return widthZ;
    }

    public double getHeight() {
        return height;
    }

}
