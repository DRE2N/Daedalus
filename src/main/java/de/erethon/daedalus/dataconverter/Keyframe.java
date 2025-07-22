package de.erethon.daedalus.dataconverter;

import de.erethon.daedalus.utils.InterpolationType;
import de.erethon.daedalus.utils.TransformationType;
import de.erethon.bedrock.chat.MessageUtil;

import java.util.List;
import java.util.Map;

public class Keyframe {
    private final TransformationType transformationType;
    private final int timeInTicks;
    private final InterpolationType interpolationType;
    private final float dataX;
    private final float dataY;
    private final float dataZ;

    public Keyframe(Object object, String modelName, String animationName) {
        Map<String, Object> data = (Map<String, Object>) object;
        transformationType = TransformationType.valueOf(((String) data.get("channel")).toUpperCase());
        interpolationType = InterpolationType.valueOf(((String) data.get("interpolation")).toUpperCase());
        timeInTicks = (int) (20 * (double) data.get("time"));
        Map<String, Object> dataPoints = ((List<Map<String, Object>>) data.get("data_points")).get(0);

        dataX = tryParseFloat(dataPoints.get("x"), modelName, animationName);
        dataY = tryParseFloat(dataPoints.get("y"), modelName, animationName);
        dataZ = tryParseFloat(dataPoints.get("z"), modelName, animationName);
    }

    private float tryParseFloat(Object rawObject, String modelName, String animationName) {
        if (!(rawObject instanceof String rawValue)) return ((Double) rawObject).floatValue();
        rawValue = rawValue.replaceAll("\\n", "");
        if (rawValue.isEmpty()) return transformationType == TransformationType.SCALE ? 1f : 0f;
        try {
            return (float) Double.parseDouble(rawValue);
        } catch (Exception e) {
            MessageUtil.log("Failed to parse supposed number value " + rawValue + " in animation " + animationName + " for model " + modelName + "!");
            return 0;
        }
    }

    // Getters

    public TransformationType getTransformationType() {
        return transformationType;
    }

    public int getTimeInTicks() {
        return timeInTicks;
    }

    public InterpolationType getInterpolationType() {
        return interpolationType;
    }

    public float getDataX() {
        return dataX;
    }

    public float getDataY() {
        return dataY;
    }

    public float getDataZ() {
        return dataZ;
    }
}
