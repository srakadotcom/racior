package pl.memexurer.racior.lookup;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class StreamLookupHandler {

  public static List<String> find(InputStream stream, int hashCode) throws IOException {
    List<String> list = new ArrayList<>();

    try (DataInputStream inputStream1 = new DataInputStream(stream)) {
      while (true) {
        try {
          int key = inputStream1.readInt();
          String value = inputStream1.readUTF();
          if (key == hashCode) {
            list.add(value);
          }
        } catch (EOFException ignored) {
          break;
        }
      }
    }

    System.out.println(list);
    return list;
  }

}
