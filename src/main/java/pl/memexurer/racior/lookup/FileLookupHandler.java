package pl.memexurer.racior.lookup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FileLookupHandler implements LookupHandler<String, List<String>> {

  private final File file;

  public FileLookupHandler(File file) {
    this.file = file;
  }

  public File getFile() {
    return file;
  }

  public List<String> find(String name) throws IOException {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      return StreamLookupHandler.find(inputStream, name.hashCode());
    }
  }

  @Override
  public String toString() {
    return "Plik: " + file;
  }
}
