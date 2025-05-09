package fun.feellmoose.muti;

import fun.feellmoose.core.Game;

public interface Repo<T extends Repo.Identified<T>> {

    interface Identified<T extends Identified<T>>{
        String getId();
    }

    void save(T serializable);
    T fetch(String id);
    void remove(String id);
    void shutdown();
}
