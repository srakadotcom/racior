package pl.memexurer.racior.lookup;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class URLLookupHandler implements LookupHandler<String, List<String>>{
  private final String url;
  private final OkHttpClient httpClient;

  public URLLookupHandler(String url, OkHttpClient httpClient) {
    this.url = url;
    this.httpClient = httpClient;
  }

  @Override
  public CompletableFuture<List<String>> findAsync(String key, ExecutorService service) {
    CompletableFuture<List<String>> completableFuture = new CompletableFuture<>();

    httpClient.newCall(new Request.Builder()
        .url(url)
        .build()).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        completableFuture.completeExceptionally(e);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        completableFuture.complete(StreamLookupHandler.find(response.body().byteStream(), key.hashCode()));
      }
    });

    return completableFuture;
  }

  @Override
  public String toString() {
    return url;
  }
}
