package game;

import java.util.Random;

public class Generate {
    public static final Random random = new Random();

    public static float randomFloat() {
        return random.nextFloat();
    }

    public static float randomFloat(float min, float max) {
        return random.nextFloat() * (max - min) + min;
    }

    public static int randomInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public static boolean randomBoolean() {
        return random.nextBoolean();
    }
}
