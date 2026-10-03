package dev.learning.compose;

/** K13：没有可空注解的旧 Java API。 */
public final class LegacyNames {
    private LegacyNames() {}
    public static String optionalName(boolean available) {
        return available ? "小明" : null;
    }
}
