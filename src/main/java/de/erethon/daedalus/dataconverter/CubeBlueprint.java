package de.erethon.daedalus.dataconverter;

import de.erethon.daedalus.utils.MathToolkit;
import de.erethon.bedrock.chat.MessageUtil;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CubeBlueprint {
    private final Map<String, Object> cubeJSON;
    private Vector3f to;
    private Vector3f from;
    private boolean validatedData = false;
    private Vector3f boneOffset = new Vector3f();
    private Boolean textureDataExists = null;

    public CubeBlueprint(double projectResolution, Map<String, Object> cubeJSON, String modelName) {
        this.cubeJSON = cubeJSON;
        //Sanitize data from ModelEngine which is not used by Minecraft resource packs
        cubeJSON.remove("rescale");
        cubeJSON.remove("locked");
        cubeJSON.remove("type");
        cubeJSON.remove("uuid");
        cubeJSON.remove("color");
        cubeJSON.remove("autouv");
        cubeJSON.remove("name");
        cubeJSON.remove("box_uv");
        cubeJSON.remove("render_order");
        cubeJSON.remove("allow_mirror_modeling");
        //process face textures
        processFace(projectResolution, (Map<String, Object>) cubeJSON.get("faces"), "north", modelName);
        processFace(projectResolution, (Map<String, Object>) cubeJSON.get("faces"), "east", modelName);
        processFace(projectResolution, (Map<String, Object>) cubeJSON.get("faces"), "south", modelName);
        processFace(projectResolution, (Map<String, Object>) cubeJSON.get("faces"), "west", modelName);
        processFace(projectResolution, (Map<String, Object>) cubeJSON.get("faces"), "up", modelName);
        processFace(projectResolution, (Map<String, Object>) cubeJSON.get("faces"), "down", modelName);

        ArrayList<Double> fromList = (ArrayList<Double>) cubeJSON.get("from");
        if (fromList == null) return;
        from = new Vector3f(
                MathToolkit.roundFourDecimalPlaces(fromList.get(0).floatValue() * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER),
                MathToolkit.roundFourDecimalPlaces(fromList.get(1).floatValue() * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER),
                MathToolkit.roundFourDecimalPlaces(fromList.get(2).floatValue() * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER));
        ArrayList<Double> toList = (ArrayList<Double>) cubeJSON.get("to");
        if (toList == null) return;
        to = new Vector3f(
                MathToolkit.roundFourDecimalPlaces(toList.get(0).floatValue() * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER),
                MathToolkit.roundFourDecimalPlaces(toList.get(1).floatValue() * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER),
                MathToolkit.roundFourDecimalPlaces(toList.get(2).floatValue() * BoneBlueprint.ARMOR_STAND_HEAD_SIZE_MULTIPLIER));
        validatedData = true;
    }

    private void processFace(double projectResolution, Map<String, Object> map, String faceName, String modelName) {
        setTextureData(projectResolution, (Map<String, Object>) map.get(faceName), modelName);
    }

    private void setTextureData(double projectResolution, Map<String, Object> map, String modelName) {
        if (map == null || map.get("texture") == null) {
            if (textureDataExists != null && textureDataExists)
                MessageUtil.log("A cube in the model " + modelName + " has a face which does not have a texture while the rest of the cube has a texture. Minecraft does not allow this. Go through every cube in that model and make sure they all either have or do not have textures on all faces, but don't mix having and not having textures for the same cube. The model will appear with the debug black and purple cube texture until fixed.");
            textureDataExists = false;
            return;
        }
        if (textureDataExists != null && !textureDataExists)
            MessageUtil.log("A cube in the model " + modelName + " has a face which does not have a texture while the rest of the cube has a texture. Minecraft does not allow this. Go through every cube in that model and make sure they all either have or do not have textures on all faces, but don't mix having and not having textures for the same cube. The model will appear with the debug black and purple cube texture until fixed.");
        textureDataExists = true;
        Double textureDouble = (Double) map.get("texture");
        int textureValue = (int) Math.round(textureDouble);
        map.put("texture", "#" + textureValue);
        map.put("tintindex", 0);
        map.putIfAbsent("rotation", 0);
        ArrayList<Double> originalUV = (ArrayList<Double>) map.get("uv");
        //For some reason Minecraft really wants images to be 16x16 so here we scale the UV to fit that
        double uvMultiplier = 16 / projectResolution;
        map.put("uv", List.of(
                MathToolkit.roundFourDecimalPlaces(originalUV.get(0) * uvMultiplier),
                MathToolkit.roundFourDecimalPlaces(originalUV.get(1) * uvMultiplier),
                MathToolkit.roundFourDecimalPlaces(originalUV.get(2) * uvMultiplier),
                MathToolkit.roundFourDecimalPlaces(originalUV.get(3) * uvMultiplier)));
    }

    public void shiftPosition() {
        from.sub(boneOffset);
        to.sub(boneOffset);
        cubeJSON.put("from", List.of(from.get(0), from.get(1), from.get(2)));
        cubeJSON.put("to", List.of(to.get(0), to.get(1), to.get(2)));
    }

    public void shiftRotation() {
        if (cubeJSON.get("origin") == null) return;
        Map<String, Object> newRotationData = new HashMap<>();

        double scaleFactor = 0.4;

        //Adjust the origin
        double xOrigin, yOrigin, zOrigin;
        List<Double> originData = (ArrayList<Double>) cubeJSON.get("origin");
        xOrigin = originData.get(0) * scaleFactor - boneOffset.get(0);
        yOrigin = originData.get(1) * scaleFactor - boneOffset.get(1);
        zOrigin = originData.get(2) * scaleFactor - boneOffset.get(2);

        double angle = 0;
        String axis = "x";

        if (cubeJSON.get("rotation") instanceof List) {
            List<Double> rotations = (List<Double>) cubeJSON.get("rotation");
            // Your logic for finding the single rotation axis seems correct for your data source.
            for (int i = rotations.size() - 1; i >= 0; i--) {
                if (rotations.get(i) != 0) {
                    angle =  MathToolkit.roundFourDecimalPlaces(rotations.get(i));
                    switch (i) {
                        case 0 -> axis = "x";
                        case 1 -> axis = "y";
                        case 2 -> axis = "z";
                        default -> MessageUtil.log("A cube rotation has an unknown axis index: " + i);
                    }
                }
            }
        }

        // Decompose rotation into base transform + allowed remainder
        RotationDecomposition decomp = decomposeRotation(angle);

        if (decomp.baseRotation != 0) {
            transformCubeGeometry(decomp.baseRotation, axis, xOrigin, yOrigin, zOrigin);
        }

        newRotationData.put("angle", decomp.remainder);
        newRotationData.put("axis", axis);
        newRotationData.put("origin", List.of(xOrigin, yOrigin, zOrigin));

        cubeJSON.put("rotation", newRotationData);
        cubeJSON.remove("origin");
    }

    private static class RotationDecomposition {
        double baseRotation;  // A multiple of 90 (0, 90, -90, 180)
        double remainder;     // An angle in the range [-45, 45]
    }

    private RotationDecomposition decomposeRotation(double angle) {
        RotationDecomposition result = new RotationDecomposition();
        // This mathematical approach is robust and correct.
        double baseRotation = Math.round(angle / 90.0) * 90.0;
        double remainder = angle - baseRotation;

        if (baseRotation == -180) {
            baseRotation = 180;
        }

        result.baseRotation = baseRotation;
        result.remainder = remainder;
        return result;
    }

    /**
     * Transforms cube geometry to handle rotations not natively supported by Minecraft's "rotation" tag.
     * This works by "baking" a 90 or 180-degree rotation directly into the cube's vertices (`from`/`to`)
     * and face definitions. The remaining small angle ([-45, 45]) is then handled by Minecraft's native rotation.
     *
     * Examples:
     * - 67.5° on Y -> 90° (geometry transform) + -22.5° (native rotation)
     * - 135° on Y -> 90° (geometry transform) + 45° (native rotation)
     */
    private void transformCubeGeometry(double angle, String axis, double originX, double originY, double originZ) {
        float fromX = from.x, fromY = from.y, fromZ = from.z;
        float toX = to.x, toY = to.y, toZ = to.z;

        Vector3f newFrom = new Vector3f();
        Vector3f newTo = new Vector3f();

        fromX -= originX; fromY -= originY; fromZ -= originZ;
        toX -= originX; toY -= originY; toZ -= originZ;

        switch (axis.toLowerCase()) {
            case "x" -> {
                if (Math.abs(angle - 90) < 0.01) { newFrom.set(fromX, -fromZ, fromY); newTo.set(toX, -toZ, toY); }
                else if (Math.abs(angle + 90) < 0.01) { newFrom.set(fromX, fromZ, -fromY); newTo.set(toX, toZ, -toY); }
                else if (Math.abs(angle - 180) < 0.01 || Math.abs(angle + 180) < 0.01) { newFrom.set(fromX, -fromY, -fromZ); newTo.set(toX, -toY, -toZ); }
            }
            case "y" -> {
                if (Math.abs(angle - 90) < 0.01) { newFrom.set(fromZ, fromY, -fromX); newTo.set(toZ, toY, -toX); }
                else if (Math.abs(angle + 90) < 0.01) { newFrom.set(-fromZ, fromY, fromX); newTo.set(-toZ, toY, toX); }
                else if (Math.abs(angle - 180) < 0.01 || Math.abs(angle + 180) < 0.01) { newFrom.set(-fromX, fromY, -fromZ); newTo.set(-toX, toY, -toZ); }
            }
            case "z" -> {
                if (Math.abs(angle - 90) < 0.01) { newFrom.set(-fromY, fromX, fromZ); newTo.set(-toY, toX, toZ); }
                else if (Math.abs(angle + 90) < 0.01) { newFrom.set(fromY, -fromX, fromZ); newTo.set(toY, -toX, toZ); }
                else if (Math.abs(angle - 180) < 0.01 || Math.abs(angle + 180) < 0.01) { newFrom.set(-fromX, -fromY, fromZ); newTo.set(-toX, -toY, toZ); }
            }
        }

        newFrom.add((float)originX, (float)originY, (float)originZ);
        newTo.add((float)originX, (float)originY, (float)originZ);

        from.set(Math.min(newFrom.x, newTo.x), Math.min(newFrom.y, newTo.y), Math.min(newFrom.z, newTo.z));
        to.set(Math.max(newFrom.x, newTo.x), Math.max(newFrom.y, newTo.y), Math.max(newFrom.z, newTo.z));

        cubeJSON.put("from", List.of(from.get(0), from.get(1), from.get(2)));
        cubeJSON.put("to", List.of(to.get(0), to.get(1), to.get(2)));

        remapFaces(angle, axis);
    }

    private void remapFaces(double angle, String axis) {
        Map<String, Object> faces = (Map<String, Object>) cubeJSON.get("faces");
        if (faces == null) return;

        Map<String, Object> originalFaces = new HashMap<>();
        for (Map.Entry<String, Object> entry : faces.entrySet()) {
            if (entry.getValue() instanceof Map) {
                originalFaces.put(entry.getKey(), new HashMap<>((Map<String, Object>) entry.getValue()));
            }
        }

        // Remapping which face is which (e.g. north becomes east)
        switch (axis.toLowerCase()) {
            case "x":
                if (Math.abs(angle - 90) < 0.01) {
                    faces.put("up", originalFaces.get("north")); faces.put("south", originalFaces.get("up")); faces.put("down", originalFaces.get("south")); faces.put("north", originalFaces.get("down"));
                    rotateFaceUV(faces, "east", 90); rotateFaceUV(faces, "west", -90);
                } else if (Math.abs(angle + 90) < 0.01) {
                    faces.put("down", originalFaces.get("north")); faces.put("north", originalFaces.get("up")); faces.put("up", originalFaces.get("south")); faces.put("south", originalFaces.get("down"));
                    rotateFaceUV(faces, "east", -90); rotateFaceUV(faces, "west", 90);
                } else if (Math.abs(angle - 180) < 0.01 || Math.abs(angle + 180) < 0.01) {
                    faces.put("south", originalFaces.get("north")); faces.put("north", originalFaces.get("south")); faces.put("down", originalFaces.get("up")); faces.put("up", originalFaces.get("down"));
                    rotateFaceUV(faces, "east", 180); rotateFaceUV(faces, "west", 180);
                }
                break;
            case "y":
                if (Math.abs(angle - 90) < 0.01) {
                    faces.put("east", originalFaces.get("north")); faces.put("south", originalFaces.get("east")); faces.put("west", originalFaces.get("south")); faces.put("north", originalFaces.get("west"));
                    rotateFaceUV(faces, "up", 90); rotateFaceUV(faces, "down", -90);
                } else if (Math.abs(angle + 90) < 0.01) {
                    faces.put("west", originalFaces.get("north")); faces.put("north", originalFaces.get("east")); faces.put("east", originalFaces.get("south")); faces.put("south", originalFaces.get("west"));
                    rotateFaceUV(faces, "up", -90); rotateFaceUV(faces, "down", 90);
                } else if (Math.abs(angle - 180) < 0.01 || Math.abs(angle + 180) < 0.01) {
                    faces.put("south", originalFaces.get("north")); faces.put("north", originalFaces.get("south")); faces.put("west", originalFaces.get("east")); faces.put("east", originalFaces.get("west"));
                    rotateFaceUV(faces, "up", 180); rotateFaceUV(faces, "down", 180);
                }
                break;
            case "z":
                if (Math.abs(angle - 90) < 0.01) {
                    faces.put("east", originalFaces.get("up")); faces.put("down", originalFaces.get("east")); faces.put("west", originalFaces.get("down")); faces.put("up", originalFaces.get("west"));
                    rotateFaceUV(faces, "north", 90); rotateFaceUV(faces, "south", -90);
                } else if (Math.abs(angle + 90) < 0.01) {
                    faces.put("west", originalFaces.get("up")); faces.put("up", originalFaces.get("east")); faces.put("east", originalFaces.get("down")); faces.put("down", originalFaces.get("west"));
                    rotateFaceUV(faces, "north", -90); rotateFaceUV(faces, "south", 90);
                } else if (Math.abs(angle - 180) < 0.01 || Math.abs(angle + 180) < 0.01) {
                    faces.put("down", originalFaces.get("up")); faces.put("up", originalFaces.get("down")); faces.put("west", originalFaces.get("east")); faces.put("east", originalFaces.get("west"));
                    rotateFaceUV(faces, "north", 180); rotateFaceUV(faces, "south", 180);
                }
                break;
        }
    }

    private void rotateFaceUV(Map<String, Object> faces, String faceName, double additionalRotation) {
        Map<String, Object> face = (Map<String, Object>) faces.get(faceName);
        if (face == null) return;

        // Get the current rotation of the face, defaulting to 0.
        int currentRotation = 0;
        Object rotObj = face.get("rotation");
        if (rotObj instanceof Number) {
            currentRotation = ((Number) rotObj).intValue();
        }

        // Add the new rotation.
        int newRotation = currentRotation + (int)additionalRotation;

        // Normalize the rotation to the 0-359 degree range.
        newRotation = (newRotation % 360 + 360) % 360;

        // Minecraft only accepts 0, 90, 180, 270.
        if (newRotation != 0 && newRotation != 90 && newRotation != 180 && newRotation != 270) {
            MessageUtil.log("Warning: Invalid final UV rotation of " + newRotation + " calculated for face " + faceName);
            return;
        }

        face.put("rotation", newRotation);
    }

    public Vector3f getTo() {
        return to;
    }

    public Vector3f getFrom() {
        return from;
    }

    public Map<String, Object> getCubeJSON() {
        return cubeJSON;
    }

    public Vector3f getBoneOffset() {
        return boneOffset;
    }

    public void setBoneOffset(Vector3f boneOffset) {
        this.boneOffset = boneOffset;
    }

    public boolean isValidatedData() {
        return validatedData;
    }

    public String getCubeName() {
        return (String) cubeJSON.get("name");
    }
}