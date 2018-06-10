package hoshisugi.rukoru.app.services.settings;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hoshisugi.rukoru.app.enums.Preferences;
import hoshisugi.rukoru.app.models.ds.DSSetting;
import hoshisugi.rukoru.app.models.scrum.ToolButton;
import hoshisugi.rukoru.app.models.settings.Credential;
import hoshisugi.rukoru.app.models.settings.Preference;
import hoshisugi.rukoru.app.models.settings.RepositoryDBConnection;
import hoshisugi.rukoru.app.models.settings.S3VideoCredential;

public interface LocalSettingService {

	void saveCredential(final Credential entity) throws Exception;

	Optional<Credential> loadCredential() throws SQLException;

	void saveRepositoryDBConnection(final RepositoryDBConnection entity) throws Exception;

	Optional<RepositoryDBConnection> loadRepositoryDBConnection() throws SQLException;

	void saveDSSettings(List<DSSetting> settings) throws SQLException;

	List<DSSetting> loadDSSettings() throws SQLException;

	Map<String, Preference> getPreferencesByCategory(String category) throws SQLException;

	Optional<Preference> findPreference(Preferences preference) throws SQLException;

	void savePreferences(Collection<Preference> preferences) throws SQLException;

	void savePreference(Preference preference) throws SQLException;

	Optional<S3VideoCredential> loadS3VideoCredential() throws SQLException;

	void saveS3VideoCredential(S3VideoCredential credential) throws Exception;

	List<ToolButton> getToolButtons() throws SQLException;

	void saveToolButtons(List<ToolButton> entities) throws SQLException;
}