package pl.memexurer.racior.lookup;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface LookupHandler<K, V> {

  default CompletableFuture<V> findAsync(K key, ExecutorService service) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return find(key);
      } catch (Exception throwable) {
        throw new RuntimeException(throwable);
      }
    }, service);
  }

  default V find(K key) throws Exception {
    throw new UnsupportedOperationException();
  }
}
