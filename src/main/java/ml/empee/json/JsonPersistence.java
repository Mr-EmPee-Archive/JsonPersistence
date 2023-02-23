package ml.empee.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ml.empee.json.adapters.LocationAdapter;
import ml.empee.json.validator.Validation;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonPersistence {
  private final Gson gson;
  public JsonPersistence(Object... adapters) {
    GsonBuilder builder = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Location.class, new LocationAdapter())
        .setPrettyPrinting()
        .disableHtmlEscaping();

    for(Object adapter : adapters) {
      builder.registerTypeAdapter(adapter.getClass(), adapter);
    }

    this.gson = builder.create();
  }

  public synchronized void serialize(File target, Object object) {
    target.getParentFile().mkdirs();
    try (BufferedWriter w = Files.newBufferedWriter(target.toPath())) {
      w.append(gson.toJson(object));
    } catch (IOException e) {
      throw new JsonParseException("Error while serializing " + target, e);
    }
  }

  @Nullable
  public <T> T deserialize(File source, Class<T> clazz) {
    try {
      T object = gson.fromJson(Files.newBufferedReader(source.toPath()), clazz);
      Validation.validate(object);
      return object;
    } catch (NoSuchFileException e) {
      return null;
    } catch (JsonParseException e) {
      throw new JsonParseException("Misconfiguration of the file " + source, e);
    } catch (IOException e) {
      throw new JsonParseException("Error while deserializing the source " + source.toPath(), e);
    }
  }

  @NotNull
  public <T> List<T> deserializeList(File source, Class<T[]> clazz) {
    T[] result = deserialize(source, clazz);
    if(result == null) {
      return new ArrayList<>();
    }

    return Arrays.asList(result);
  }

}
