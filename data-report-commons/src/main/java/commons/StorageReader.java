package commons;

@FunctionalInterface
public interface StorageReader<T> {
  String read(T blob);
}
