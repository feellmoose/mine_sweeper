package fun.feellmoose.muti;

public interface TimeMachineRepo<T extends Repo.Identified<T>> extends Repo<T> {
    T cancel(int steps);
}
