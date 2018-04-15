package hoshisugi.rukoru.app.view.ds;

import static hoshisugi.rukoru.framework.util.AssetUtil.getImage;
import static javafx.beans.binding.Bindings.when;

import java.net.URL;
import java.util.ResourceBundle;

import com.google.inject.Inject;

import hoshisugi.rukoru.app.models.ds.DSLogWriter;
import hoshisugi.rukoru.app.models.ds.DSSetting;
import hoshisugi.rukoru.app.services.ds.DSService;
import hoshisugi.rukoru.framework.base.BaseController;
import hoshisugi.rukoru.framework.cli.CLI;
import hoshisugi.rukoru.framework.cli.CLIState;
import hoshisugi.rukoru.framework.util.AssetUtil;
import hoshisugi.rukoru.framework.util.ConcurrentUtil;
import hoshisugi.rukoru.framework.util.FXUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

public class DSEntryController extends BaseController {

	@FXML
	private Accordion accordion;

	@FXML
	private TitledPane titledPane;

	@FXML
	private Button openHomeButton;

	@FXML
	private Label name;

	@FXML
	private ToggleButton controlServerButton;

	@FXML
	private ToggleButton controlStudioButton;

	@FXML
	private ToggleButton controlAllButton;

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab serverConsole;

	@FXML
	private Tab studioConsole;

	@FXML
	private TextArea serverLogText;

	@FXML
	private TextArea studioLogText;

	@FXML
	private TextField port;

	@FXML
	private Button changePortButton;

	@Inject
	private DSService service;

	private DSSetting dsSetting;

	private final EventHandler<WindowEvent> stopOnExit = e -> this.stopOnExit();

	public Integer getDSSettingId() {
		return dsSetting.getId();
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		openHomeButton.setGraphic(new ImageView(AssetUtil.getImage("32x32/folder.png")));
		controlServerButton.graphicProperty().bind(when(controlServerButton.selectedProperty().not())
				.then(new ImageView(getImage("32x32/run.png"))).otherwise(new ImageView(getImage("32x32/stop.png"))));
		controlStudioButton.graphicProperty().bind(when(controlStudioButton.selectedProperty().not())
				.then(new ImageView(getImage("32x32/run.png"))).otherwise(new ImageView(getImage("32x32/stop.png"))));
		controlAllButton.graphicProperty().bind(when(controlAllButton.selectedProperty().not())
				.then(new ImageView(getImage("32x32/run.png"))).otherwise(new ImageView(getImage("32x32/stop.png"))));
		controlAllButton.selectedProperty()
				.bind(controlServerButton.selectedProperty().or(controlStudioButton.selectedProperty()));
		controlAllButton.disableProperty()
				.bind(controlServerButton.disabledProperty().or(controlStudioButton.disabledProperty()));
	}

	@FXML
	private void onOpenHomeButtonClick(final ActionEvent event) {
		CLI.command("explorer").options(dsSetting.getExecutionPath()).execute();
	}

	@FXML
	private void onControlServerButtonClick(final ActionEvent event) {
		if (accordion.getExpandedPane() == null) {
			accordion.setExpandedPane(titledPane);
		}
		final SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		if (selectionModel.getSelectedItem() != serverConsole) {
			selectionModel.select(serverConsole);
		}
		controlServerButton.setDisable(true);
		if (controlServerButton.isSelected()) {
			serverLogText.clear();
			ConcurrentUtil.run(() -> {
				final String executionType = dsSetting.getExecutionType();
				switch (executionType) {
				case "EXE":
					service.startServerExe(dsSetting, new DSLogWriter(serverLogText), this::onServerStarted);
					break;
				case "SERVICE":
					service.startServerService(dsSetting, new DSLogWriter(serverLogText), this::onServerStarted);
					break;
				case "BAT":
					service.startServerBat(dsSetting, new DSLogWriter(serverLogText), this::onServerStarted);
					break;
				}
			});
		} else {
			ConcurrentUtil.run(() -> {
				final String executionType = dsSetting.getExecutionType();
				switch (executionType) {
				case "EXE":
					service.stopServerExe(dsSetting, this::onServerStopped);
					break;
				case "SERVICE":
					service.stopServerService(dsSetting, this::onServerStopped);
					break;
				case "BAT":
					service.stopServerBat(dsSetting, this::onServerStopped);
					break;
				}
			});
		}
	}

	@FXML
	private void onControlStudioButtonClick(final ActionEvent event) {
		if (accordion.getExpandedPane() == null) {
			accordion.setExpandedPane(titledPane);
		}
		final SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		if (selectionModel.getSelectedItem() != studioConsole) {
			selectionModel.select(studioConsole);
		}
		controlStudioButton.setDisable(true);
		if (controlStudioButton.isSelected()) {
			studioLogText.clear();
			ConcurrentUtil.run(() -> {
				final String executionType = dsSetting.getExecutionType();
				switch (executionType) {
				case "EXE":
				case "SERVICE":
					service.startStudioExe(dsSetting, new DSLogWriter(studioLogText), this::onStudioStarted);
					break;
				case "BAT":
					service.startStudioBat(dsSetting, new DSLogWriter(studioLogText), this::onStudioStarted);
					break;
				}
			});
		} else {
			ConcurrentUtil.run(() -> {
				final String executionType = dsSetting.getExecutionType();
				switch (executionType) {
				case "EXE":
				case "SERVICE":
					service.stopStudioExe(dsSetting, this::onStudioStopped);
					break;
				case "BAT":
					service.stopStudioBat(dsSetting, this::onStudioStopped);
					break;
				}
			});
		}
	}

	@FXML
	private void onControlAllButtonClick(final ActionEvent event) {
		System.out.println("onControlAllButtonClick");
	}

	@FXML
	private void onChangePortButtonClick(final ActionEvent event) {
		ConcurrentUtil.run(() -> {
			service.changePort(dsSetting, port.getText());
			Platform.runLater(() -> FXUtil.showTooltip("ポートを変更しました。", event));
		});
	}

	public void loadSetting(final DSSetting dsSetting) {
		Platform.runLater(() -> {
			name.setText(dsSetting.getName());
			port.setText(dsSetting.getPort());
			controlServerButton.setDisable(!dsSetting.isServerInstalled());
			controlStudioButton.setDisable(!dsSetting.isStudioInstalled());
			controlServerButton.setSelected(service.isServerRunning(dsSetting));
			controlStudioButton.setSelected(service.isStudioRunning(dsSetting));
			port.setDisable(!dsSetting.isServerInstalled());
			changePortButton.setDisable(!dsSetting.isStudioInstalled());
		});
		this.dsSetting = dsSetting;
	}

	private void onServerStarted(final CLIState state) {
		if (controlServerButton.isDisable()) {
			Platform.runLater(() -> controlServerButton.setDisable(false));
		}
		if (state != null && state.isSuccess()) {
			FXUtil.getPrimaryStage().setOnCloseRequest(stopOnExit);
		}
		final boolean succeeded = state == null || state.isSuccess();
		Platform.runLater(() -> controlServerButton.setSelected(succeeded));
	}

	private void onServerStopped(final CLIState state) {
		if (controlServerButton.isDisable()) {
			Platform.runLater(() -> controlServerButton.setDisable(false));
		}
		final boolean selected = state != null && !state.isSuccess();
		Platform.runLater(() -> controlServerButton.setSelected(selected));
	}

	private void onStudioStarted(final CLIState state) {
		if (controlStudioButton.isDisable()) {
			Platform.runLater(() -> controlStudioButton.setDisable(false));
		}
		final boolean selected = state == null || !state.isSuccess();
		Platform.runLater(() -> controlStudioButton.setSelected(selected));
	}

	private void onStudioStopped(final CLIState state) {
		if (controlStudioButton.isDisable()) {
			Platform.runLater(() -> controlStudioButton.setDisable(false));
		}
		final boolean selected = state != null && !state.isSuccess();
		Platform.runLater(() -> controlStudioButton.setSelected(selected));
	}

	private void stopOnExit() {
		if (controlServerButton.isSelected()) {
			ConcurrentUtil.run(() -> service.stopServerExe(dsSetting, this::onServerStopped));
		}
	}
}
