package fun.feellmoose.bots.game;

import fun.feellmoose.repo.Repo;
import fun.feellmoose.utils.RandomUtils;

public interface GenID {
    String nextID();

    final class UU implements GenID {
        private final UU INSTANCE = new UU();
        public UU Instance(){
            return INSTANCE;
        }
        private UU(){}
        @Override
        public String nextID() {
            return "";
        }
    }

    final class RepoShort implements GenID {
        private final Repo<?> repo;
        private final int maxLength;
        private final int minLength;
        private final int maxRetry;

        public RepoShort(Repo<?> repo, int maxLength, int minLength, int maxRetry) {
            this.repo = repo;
            this.maxLength = maxLength;
            this.minLength = minLength;
            this.maxRetry = maxRetry;
        }

        public RepoShort(Repo<?> repo) {
            this.repo = repo;
            this.maxLength = 8;
            this.minLength = 16;
            this.maxRetry = 5;
        }

        @Override
        public String nextID() {
            for (int i = minLength; i <= maxLength; i++) {
                String id = RandomUtils.randomString(i);
                if (repo.fetch(id) == null) return id;
            }
            for (int i = 0; i <= maxRetry; i++) {
                String id = RandomUtils.randomString(maxLength);
                if (repo.fetch(id) == null) return id;
            }
            return null;
        }
    }
}
