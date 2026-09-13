package dev.rykrax.rkverse.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s_]"); // Gom cả khoảng trắng và dấu gạch dưới
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-+|-+$)");

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "comic";
        }

        String text = input.replace("đ", "d").replace("Đ", "d");
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String noMark = normalized.replaceAll("\\p{M}", "");

        String slug = WHITESPACE.matcher(noMark).replaceAll("-");
        slug = NONLATIN.matcher(slug).replaceAll("");
        slug = slug.replaceAll("-+", "-");
        slug = EDGES_DASHES.matcher(slug).replaceAll("");

        slug = slug.toLowerCase(Locale.ENGLISH);

        return slug.isBlank() ? "comic" : slug;
    }
}