package best.lettuce.config;

import best.lettuce.Lettuce;
import best.lettuce.utils.non_utils.drag.Dragging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class DragManager {
    public static HashMap<String, Dragging> draggables = new HashMap<>();

    private static final File DRAG_DATA = new File(Lettuce.DIRECTORY, "Drag.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().setLenient().create();

    public static void saveDragData() {
        if (!DRAG_DATA.exists()) {
            DRAG_DATA.getParentFile().mkdirs();
        }
        try {
            Files.writeString(DRAG_DATA.toPath(), GSON.toJson(draggables.values()));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Failed to save draggables");
        }
    }

    public static void loadDragData() {
        if (!DRAG_DATA.exists()) {
            System.out.println("No drag data found");
            return;
        }

        Dragging[] draggings;
        try {
            draggings = GSON.fromJson(Files.readString(DRAG_DATA.toPath()), Dragging[].class);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Failed to load draggables");
            return;
        }

        for (Dragging dragging : draggings) {
            if (!draggables.containsKey(dragging.getName())) continue;
            Dragging currentDrag = draggables.get(dragging.getName());
            currentDrag.setX(dragging.getX());
            currentDrag.setY(dragging.getY());
            draggables.put(dragging.getName(), currentDrag);
        }
    }
}