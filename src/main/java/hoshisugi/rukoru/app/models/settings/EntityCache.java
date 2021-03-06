package hoshisugi.rukoru.app.models.settings;

import java.util.Optional;
import java.util.concurrent.Callable;

public class EntityCache<T extends DBEntity> {

	private Optional<T> savedEntity = Optional.empty();
	private final Callable<Optional<T>> provider;

	public EntityCache(final Callable<Optional<T>> provider) {
		this.provider = provider;
	}

	public boolean hasEntity() {
		if (!savedEntity.isPresent()) {
			try {
				reload();
			} catch (final Exception e) {
				return false;
			}
		}
		return savedEntity.isPresent();
	}

	public T get() {
		return savedEntity.get();
	}

	public void reload() throws Exception {
		savedEntity = provider.call();
	}
}
