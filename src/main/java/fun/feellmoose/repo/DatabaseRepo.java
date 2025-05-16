package fun.feellmoose.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public abstract class DatabaseRepo<T extends Repo.Identified<T>> implements Repo<T> {
    //TODO private final SqlClient client
    private final ObjectMapper mapper = new ObjectMapper();

    abstract Class<T> getType();


    @Override
    @SneakyThrows
    public void save(T serializable) {
        byte[] src = mapper.writeValueAsBytes(serializable);
    }

    @Override
    @SneakyThrows
    public T fetch(String id) {
        byte[] src = new byte[]{};
        return mapper.readValue(src,getType());
    }

    @Override
    @SneakyThrows
    public void remove(String id) {

    }

    @Override
    @SneakyThrows
    public void shutdown() {
        //client shutdown
    }

}
