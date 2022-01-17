package pl.memexurer.racior.lookup;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;

public class LookupService implements LookupHandler<String, List<LookupResponse>> {

  private final ExecutorService service = Executors.newFixedThreadPool(
      Runtime.getRuntime().availableProcessors());
  private final List<LookupHandler<String, List<String>>> lookupHandlerList = new ArrayList<>();
  private final File saveFile = new File("bambik-botnet-handler.list");
  private final OkHttpClient client = new OkHttpClient();

  public void loadLookupHandlers() throws IOException {
    if(!saveFile.exists())
      return;

    Files.readAllLines(saveFile.toPath())
        .forEach(str -> {
          String[] split = str.split(":");
          registerLookupHandler(split[0], str.substring(split[0].length() + 1));
        });
  }

  public CompletableFuture<List<LookupResponse>> lookup(String lookupName) {
    Map<LookupHandler<String, List<String>>, CompletableFuture<List<String>>> completableFutures = lookupHandlerList.stream()
        .collect(Collectors.toMap(Function.identity(),
            handler -> handler.findAsync(lookupName, service)));
    return CompletableFuture.allOf(completableFutures.values().toArray(new CompletableFuture[0]))
        .thenApply(v ->
            completableFutures.entrySet()
                .stream()
                .flatMap(entry -> {
                  try {
                    return entry.getValue().get().stream()
                        .map(str -> new LookupResponse(entry.getKey().toString(), str));
                  } catch (InterruptedException | ExecutionException e) {
                    throw new Error(e);
                  }
                })
                .collect(Collectors.toList()));
  }

  public boolean isBroken() {
    return lookupHandlerList.isEmpty();
  }

  public void registerLookupHandler(String type, String url) {
    if ("file".equals(type)) {
      File file = new File(url);
      if (!file.exists()) {
        throw new IllegalArgumentException("Specified file does not exist!");
      }
      lookupHandlerList.add(new FileLookupHandler(file));
    } else if("url".equals(type)) {
      try {
        new URL(url);
      } catch (MalformedURLException ex) {
        throw new IllegalArgumentException("Invalid URL.");
      }
      lookupHandlerList.add(new URLLookupHandler(url, client));
    } else {
      throw new IllegalArgumentException("Unkonwn type " + type);
    }
    saveLookupHandlers();
  }

  private void saveLookupHandlers() {
    try {
      Files.writeString(saveFile.toPath(), lookupHandlerList.stream()
          .map(handler -> {
            if(handler instanceof FileLookupHandler fileLookupHandler)
              return "file:" + fileLookupHandler.getFile().getAbsolutePath();
            else if(handler instanceof URLLookupHandler urlLookupHandler)
              return "url:" + urlLookupHandler;
            else return null;
          }).filter(Objects::nonNull).collect(Collectors.joining("\n")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void removeService(int service) {
    if (service < 0 || service > lookupHandlerList.size()) {
      throw new IllegalArgumentException("Lookup list does not contain specified item.");
    }

    lookupHandlerList.remove(service);
    saveLookupHandlers();
  }

  public String getFormattedEntries() {
    StringBuilder numberedList = new StringBuilder();

    int counter = 0;
    for (LookupHandler<String, List<String>> lookupHandler : lookupHandlerList) {
      numberedList.append(counter++).append(". ").append(lookupHandler).append('\n');
    }

    return numberedList.toString();
  }

}
