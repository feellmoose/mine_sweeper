package fun.feellmoose.repo;

public interface Repo<T extends Repo.Identified<T>> {

    interface Identified<T extends Identified<T>>{
        String id();
    }

    void save(T serializable);
    T fetch(String id);
    void remove(String id);
    void shutdown();
}
