package ml.empee.json;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class JsonRepository<Type extends Cloneable> {
  private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(getClass());
  private final List<Type> data = new ArrayList<>();
  private final JsonPersistence persistence;
  private final File dataFile;

  public JsonRepository(String dataPath, Class<Type[]> dataType, Object... adapters) {
    this.dataFile = new File(plugin.getDataFolder(), dataPath);
    this.persistence = new JsonPersistence(adapters);

    Type[] data = persistence.deserialize(dataFile, dataType);
    if (data != null) {
      this.data.addAll(Arrays.asList(data));
    }
  }

  public void saveDB() {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> persistence.serialize(data, dataFile));
  }

  public List<Type> findAll() {
    return Collections.unmodifiableList(data);
  }

  public void forEach(Consumer<Type> consumer) {
    data.forEach(consumer);
  }

  public void save(Type type) {
    data.add(type);
    saveDB();
  }

  public void saveAll(List<Type> types) {
    data.addAll(types);
    saveDB();
  }

  public void remove(Type type) {
    data.remove(type);
    saveDB();
  }

  public void removeAll(List<Type> types) {
    data.removeAll(types);
    saveDB();
  }

  public void clear() {
    data.clear();
    saveDB();
  }

  public boolean contains(Type type) {
    return data.contains(type);
  }

  public int size() {
    return data.size();
  }

  public boolean isEmpty() {
    return data.isEmpty();
  }

  public void removeIf(Predicate<Type> filter) {
    data.removeIf(filter);
    saveDB();
  }

  public Stream<Type> stream() {
    return data.stream();
  }

}
