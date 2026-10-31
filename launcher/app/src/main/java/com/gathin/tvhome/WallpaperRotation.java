package com.gathin.tvhome;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

final class WallpaperRotation {
    /** Playback orders persisted in config.json (wallpaper_order). */
    static final String ORDER_SEQUENTIAL = "sequential";
    static final String ORDER_SHUFFLE = "shuffle";
    /** Every picture once per round, in a new shuffled order each round (wallpaper_shuffle "bag"). */
    static final String ORDER_BAG = "bag";

    static boolean validOrder(String order) {
        return ORDER_SEQUENTIAL.equals(order) || ORDER_SHUFFLE.equals(order) || ORDER_BAG.equals(order);
    }

    /**
     * wallpaper_shuffle, when a theme sets it, in wallpaper_order's terms: none plays the folder in order, random is
     * the historical shuffle, bag the round-based one. Anything else (or no key) keeps {@code order}.
     */
    static String orderFor(String shuffle, String order) {
        if ("none".equals(shuffle)) return ORDER_SEQUENTIAL;
        if ("random".equals(shuffle)) return ORDER_SHUFFLE;
        if (ORDER_BAG.equals(shuffle)) return ORDER_BAG;
        return order;
    }

    /** A bag round: the shuffled names of the collection and the position of the picture on screen. */
    static final class Bag {
        final List<String> order;
        final int position;

        Bag(List<String> order, int position) {
            this.order = order;
            this.position = position;
        }

        String current() {
            return order.get(position);
        }

        /** The round as stored in the rotation's preferences: one name per line. */
        String encoded() {
            return String.join("\n", order);
        }

        /** A stored round, or null when there is none or its position does not fit it. */
        static Bag decode(String encoded, int position) {
            if (encoded == null || encoded.isEmpty()) return null;
            List<String> order = Arrays.asList(encoded.split("\n"));
            return position >= 0 && position < order.size() ? new Bag(order, position) : null;
        }
    }

    /**
     * The step after {@code bag}: the next picture of the round, or the first of a newly shuffled round once every
     * picture has been on screen. A round that no longer matches the collection (pictures added or removed) is
     * replaced by a new one. A new round never opens with {@code previous}, the picture on screen, so no picture
     * shows twice in a row across the seam between two rounds. Null for an empty collection.
     */
    static Bag nextInBag(List<File> images, Bag bag, String previous, Random random) {
        List<String> names = new ArrayList<>();
        for (File image : images) names.add(image.getName());
        if (names.isEmpty()) return null;
        if (bag == null || !sameNames(bag.order, names) || bag.position + 1 >= bag.order.size()) {
            return new Bag(shuffledRound(names, previous, random), 0);
        }
        return new Bag(bag.order, bag.position + 1);
    }

    /** {@code names} in a random order whose first entry is not {@code avoidFirst} (when there is a choice). */
    static List<String> shuffledRound(List<String> names, String avoidFirst, Random random) {
        List<String> round = new ArrayList<>(names);
        Collections.shuffle(round, random);
        if (round.size() > 1 && round.get(0).equals(avoidFirst)) {
            Collections.swap(round, 0, 1 + random.nextInt(round.size() - 1));
        }
        return round;
    }

    private static boolean sameNames(List<String> a, List<String> b) {
        return a.size() == b.size() && new java.util.HashSet<>(a).equals(new java.util.HashSet<>(b));
    }

    static List<File> images(File directory) {
        List<File> result = new ArrayList<>();
        File[] entries = directory == null ? null : directory.listFiles();
        if (entries == null) return result;
        Arrays.sort(entries, Comparator.comparing(File::getName));
        for (File entry : entries) {
            String name = entry.getName().toLowerCase(Locale.ROOT);
            if (entry.isFile() && entry.length() > 0 && entry.length() <= 10 * 1024 * 1024
                    && (name.endsWith(".jpg") || name.endsWith(".jpeg")
                    || name.endsWith(".png") || name.endsWith(".webp"))) {
                result.add(entry);
                if (result.size() == 100) break;
            }
        }
        return result;
    }

    static File find(List<File> images, String name) {
        for (File image : images) if (image.getName().equals(name)) return image;
        return null;
    }

    /** The on-TV choice (Launcher settings, Wallpaper) that turns the wallpaper off. */
    static final String CHOICE_NONE = "none";

    /**
     * What an on-TV wallpaper choice puts on Home in place of the theme's rotation: null when the theme decides
     * (no choice, or a picture that is no longer there), an empty list for {@link #CHOICE_NONE}, or the one picture
     * chosen from the theme's collection or its single {@code single} wallpaper.
     */
    static List<File> chosen(List<File> collection, File single, String choice) {
        if (choice == null) return null;
        if (CHOICE_NONE.equals(choice)) return Collections.emptyList();
        File picked = find(collection, choice);
        if (picked == null && single != null && choice.equals(single.getName())) picked = single;
        return picked == null ? null : Collections.singletonList(picked);
    }

    static File pick(List<File> images, String previous, Random random) {
        List<File> candidates = new ArrayList<>(images);
        if (candidates.size() > 1) candidates.removeIf(image -> image.getName().equals(previous));
        return candidates.isEmpty() ? null : candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * The next picture in the sorted collection, wrapping to the first at the end. The list from
     * {@link #images(File)} is sorted by filename, so this is a stable order across restarts. A
     * previous name that is no longer in the collection starts again at the first picture.
     */
    static File next(List<File> images, String previous) {
        if (images.isEmpty()) return null;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getName().equals(previous)) {
                return images.get((i + 1) % images.size());
            }
        }
        return images.get(0);
    }

    /** Picks the next picture for {@code order}; an unknown order keeps the historical shuffle. */
    static File pick(List<File> images, String previous, String order, Random random) {
        return ORDER_SEQUENTIAL.equals(order) ? next(images, previous) : pick(images, previous, random);
    }

    static boolean due(long previous, long now, long interval) {
        // The minute tick is a Handler postDelayed, so it drifts by tens of milliseconds and often lands a few
        // hundred short of the interval. Compared exactly, that pass is skipped and the picture waits a whole
        // extra interval - a one-minute collection then changes every two. Two seconds of slack absorbs the
        // drift without letting a genuinely early call advance the picture.
        long slack = Math.min(2_000L, interval / 10);
        return interval > 0 && (previous == 0 || now < previous || now - previous >= interval - slack);
    }
}
