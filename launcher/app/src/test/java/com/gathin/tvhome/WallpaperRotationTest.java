package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class WallpaperRotationTest {

    private static List<File> pictures(int count) {
        List<File> files = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) files.add(new File(String.format("foto-%03d.jpg", i)));
        return files;
    }

    @Test
    public void bagShowsEveryPictureOncePerRoundAndNeverRepeatsAcrossTheSeam() {
        List<File> images = pictures(7);
        Random random = new Random(42);
        WallpaperRotation.Bag bag = null;
        String previous = "";
        for (int round = 0; round < 50; round++) {
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (int i = 0; i < images.size(); i++) {
                bag = WallpaperRotation.nextInBag(images, bag, previous, random);
                assertNotEquals("a picture twice in a row", previous, bag.current());
                assertTrue("a picture twice in one round", seen.add(bag.current()));
                previous = bag.current();
            }
            assertEquals(images.size(), seen.size());
        }
    }

    @Test
    public void bagSurvivesBeingStoredAndRestored() {
        List<File> images = pictures(5);
        Random random = new Random(7);
        WallpaperRotation.Bag bag = WallpaperRotation.nextInBag(images, null, "", random);
        bag = WallpaperRotation.nextInBag(images, bag, bag.current(), random);
        WallpaperRotation.Bag restored = WallpaperRotation.Bag.decode(bag.encoded(), bag.position);
        assertEquals(bag.order, restored.order);
        assertEquals(bag.current(), restored.current());
        // The restored round carries on where it was, it does not start over.
        WallpaperRotation.Bag next = WallpaperRotation.nextInBag(images, restored, restored.current(), random);
        assertEquals(bag.order, next.order);
        assertEquals(bag.position + 1, next.position);
        assertNull(WallpaperRotation.Bag.decode("", 0));
        assertNull(WallpaperRotation.Bag.decode(bag.encoded(), 5));
    }

    @Test
    public void bagStartsANewRoundWhenTheCollectionChanges() {
        List<File> images = pictures(5);
        Random random = new Random(3);
        WallpaperRotation.Bag bag = WallpaperRotation.nextInBag(images, null, "", random);
        bag = WallpaperRotation.nextInBag(images, bag, bag.current(), random);
        List<File> grown = pictures(6);
        WallpaperRotation.Bag next = WallpaperRotation.nextInBag(grown, bag, bag.current(), random);
        assertEquals(0, next.position);
        assertEquals(6, next.order.size());
        assertNotEquals(bag.current(), next.current());
        assertNull(WallpaperRotation.nextInBag(Collections.emptyList(), bag, "", random));
    }

    @Test
    public void shuffleKeyMapsOntoTheOrderAndLeavesOtherThemesAlone() {
        assertEquals(WallpaperRotation.ORDER_SEQUENTIAL, WallpaperRotation.orderFor("none", "shuffle"));
        assertEquals(WallpaperRotation.ORDER_SHUFFLE, WallpaperRotation.orderFor("random", "sequential"));
        assertEquals(WallpaperRotation.ORDER_BAG, WallpaperRotation.orderFor("bag", "shuffle"));
        assertEquals("sequential", WallpaperRotation.orderFor(null, "sequential"));
        assertEquals("shuffle", WallpaperRotation.orderFor("sometimes", "shuffle"));
    }

    private static File touch(File dir, String name, int bytes) throws IOException {
        File f = new File(dir, name);
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(new byte[bytes]);
        }
        return f;
    }

    @Test
    public void imagesKeepsPicturesOnlySortedByName() throws IOException {
        File dir = Files.createTempDirectory("walls").toFile();
        touch(dir, "b.JPG", 10);
        touch(dir, "a.png", 10);
        touch(dir, "notes.txt", 10);
        touch(dir, "empty.jpg", 0);
        new File(dir, "folder.jpg").mkdir();
        List<File> images = WallpaperRotation.images(dir);
        assertEquals(Arrays.asList("a.png", "b.JPG"), names(images));
        assertTrue(WallpaperRotation.images(null).isEmpty());
        assertTrue(WallpaperRotation.images(new File(dir, "missing")).isEmpty());
    }

    @Test
    public void pickAvoidsThePreviousPictureWhenThereIsAnother() {
        List<File> images = Arrays.asList(new File("a.jpg"), new File("b.jpg"));
        Random random = new Random(1);
        for (int i = 0; i < 20; i++) {
            assertNotEquals("a.jpg", WallpaperRotation.pick(images, "a.jpg", random).getName());
        }
        assertEquals("a.jpg", WallpaperRotation.pick(Collections.singletonList(new File("a.jpg")), "a.jpg", random).getName());
        assertNull(WallpaperRotation.pick(Collections.emptyList(), "", random));
    }

    @Test
    public void sequentialAdvancesInSortedOrderAndWraps() {
        List<File> images = Arrays.asList(new File("a.jpg"), new File("b.jpg"), new File("c.jpg"));
        assertEquals("b.jpg", WallpaperRotation.next(images, "a.jpg").getName());
        assertEquals("c.jpg", WallpaperRotation.next(images, "b.jpg").getName());
        assertEquals("a.jpg", WallpaperRotation.next(images, "c.jpg").getName());
        // A first run, or an image removed from the collection, starts at the first picture.
        assertEquals("a.jpg", WallpaperRotation.next(images, "").getName());
        assertEquals("a.jpg", WallpaperRotation.next(images, "gone.jpg").getName());
        assertEquals("only.jpg", WallpaperRotation.next(
                Collections.singletonList(new File("only.jpg")), "only.jpg").getName());
        assertNull(WallpaperRotation.next(Collections.emptyList(), ""));
    }

    @Test
    public void pickUsesTheRequestedOrder() {
        List<File> images = Arrays.asList(new File("a.jpg"), new File("b.jpg"), new File("c.jpg"));
        Random random = new Random(1);
        assertEquals("b.jpg", WallpaperRotation.pick(images, "a.jpg", WallpaperRotation.ORDER_SEQUENTIAL, random).getName());
        assertEquals("a.jpg", WallpaperRotation.pick(images, "c.jpg", WallpaperRotation.ORDER_SEQUENTIAL, random).getName());
        for (int i = 0; i < 20; i++) {
            assertNotEquals("a.jpg", WallpaperRotation.pick(
                    images, "a.jpg", WallpaperRotation.ORDER_SHUFFLE, random).getName());
        }
        // Unknown order keeps the historical shuffle rather than failing or blanking.
        assertNotEquals("a.jpg", WallpaperRotation.pick(images, "a.jpg", "random", random).getName());
    }

    @Test
    public void findsTheFirstPictureWhenThePreviousOneIsGone() {
        List<File> images = Arrays.asList(new File("a.jpg"), new File("b.jpg"));
        assertEquals("a.jpg", WallpaperRotation.next(images, "deleted.jpg").getName());
    }

    @Test
    public void findMatchesByName() {
        List<File> images = Arrays.asList(new File("a.jpg"), new File("b.jpg"));
        assertEquals("b.jpg", WallpaperRotation.find(images, "b.jpg").getName());
        assertNull(WallpaperRotation.find(images, "c.jpg"));
    }

    @Test
    public void anOnTvPickReplacesTheRotation() {
        List<File> collection = Arrays.asList(new File("a.jpg"), new File("b.jpg"), new File("c.jpg"));
        File single = new File("wallpaper.jpg");
        // No choice, or a picture that has gone: the theme's rotation decides.
        assertNull(WallpaperRotation.chosen(collection, single, null));
        assertNull(WallpaperRotation.chosen(collection, single, "gone.jpg"));
        assertNull(WallpaperRotation.chosen(collection, null, "wallpaper.jpg"));
        // None: no wallpaper at all.
        assertTrue(WallpaperRotation.chosen(collection, single, WallpaperRotation.CHOICE_NONE).isEmpty());
        assertTrue(WallpaperRotation.chosen(Collections.emptyList(), null, WallpaperRotation.CHOICE_NONE).isEmpty());
        // A picked picture is the only one shown, so the rotation can't move past it.
        assertEquals(Collections.singletonList("b.jpg"), names(WallpaperRotation.chosen(collection, single, "b.jpg")));
        assertEquals(Collections.singletonList("wallpaper.jpg"),
                names(WallpaperRotation.chosen(collection, single, "wallpaper.jpg")));
        // Picking again simply replaces the earlier pick.
        assertEquals(Collections.singletonList("c.jpg"), names(WallpaperRotation.chosen(collection, single, "c.jpg")));
    }

    @Test
    public void homeRebuildsWhenTheSettingsRevisionMoved() {
        assertFalse(Config.settingsChanged(0, 0));
        assertFalse(Config.settingsChanged(3, 3));
        assertTrue(Config.settingsChanged(0, 1));
        assertTrue(Config.settingsChanged(4, 0));   // settings cleared
    }

    @Test
    public void dueOnlyWithRotationOnAndTheIntervalPassed() {
        long minute = 60_000L;
        assertFalse(WallpaperRotation.due(0, 10 * minute, 0));             // rotation off
        assertTrue(WallpaperRotation.due(0, 10 * minute, 5 * minute));      // never changed yet
        assertFalse(WallpaperRotation.due(8 * minute, 10 * minute, 5 * minute));
        assertTrue(WallpaperRotation.due(5 * minute, 10 * minute, 5 * minute));
        assertTrue(WallpaperRotation.due(20 * minute, 10 * minute, 5 * minute));   // clock went back
    }

    private static List<String> names(List<File> files) {
        String[] out = new String[files.size()];
        for (int i = 0; i < out.length; i++) out[i] = files.get(i).getName();
        return Arrays.asList(out);
    }
}
