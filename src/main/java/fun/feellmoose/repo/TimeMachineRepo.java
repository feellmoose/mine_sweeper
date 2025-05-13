package fun.feellmoose.repo;

public interface TimeMachineRepo<T extends Repo.Identified<T>> extends Repo<T> {
    T cancel(int steps);
}
