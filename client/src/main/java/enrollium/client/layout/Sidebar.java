package enrollium.client.layout;

import atlantafx.base.controls.Spacer;
import atlantafx.base.theme.Styles;
import enrollium.client.Resources;
import enrollium.client.event.BrowseEvent;
import enrollium.client.event.DefaultEventBus;
import enrollium.client.event.HotkeyEvent;
import enrollium.client.util.Lazy;
import enrollium.lib.version.Version;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;

import java.net.URI;
import java.util.Objects;

import static atlantafx.base.theme.Styles.*;


final class Sidebar extends VBox {
    private final NavTree              navTree; // Displays the navigation structure built from MainModel.
    private final Lazy<SearchDialog>   searchDialog;
    private final Lazy<SettingsDialog> themeDialog;

    public Sidebar(MainModel model) {
        super();

        // navTree is initialized with the MainModel to display the navigation structure.
        this.navTree = new NavTree(model);

        createView();

        // Listens for changes in selectedPageProperty() to highlight the correct item in the sidebar.
        model.selectedPageProperty().addListener((_, _, val) -> {
            if (val != null) navTree.getSelectionModel().select(model.getTreeItemForPage(val));
        });

        // Lazy Dialog Initialization

        searchDialog = new Lazy<>(() -> {
            var dialog = new SearchDialog(model);
            dialog.setClearOnClose(true);
            return dialog;
        });

        themeDialog = new Lazy<>(() -> {
            var dialog = new SettingsDialog();
            dialog.setClearOnClose(true);
            return dialog;
        });

        // Hotkey Listeners

        DefaultEventBus.getInstance().subscribe(HotkeyEvent.class, e -> {
            if (e.getKeys().getCode() == KeyCode.SLASH) openSearchDialog();
        });

        var themeKeys = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
        DefaultEventBus.getInstance().subscribe(HotkeyEvent.class, e -> {
            if (Objects.equals(e.getKeys(), themeKeys)) openThemeDialog();
        });
    }

    private void createView() {
        var header = new Header();

        VBox.setVgrow(navTree, Priority.ALWAYS);

        setId("sidebar");
        getChildren().addAll(header, navTree, createFooter());
    }

    private HBox createFooter() {
        var versionLbl = new Label(Version.getVersion());
        versionLbl.getStyleClass().addAll("version", TEXT_SMALL, TEXT_BOLD, TEXT_SUBTLE);
        versionLbl.setCursor(Cursor.HAND);
        versionLbl.setOnMouseClicked(e -> {
            var homepage = System.getProperty("app.homepage");
            if (homepage != null) DefaultEventBus.getInstance().publish(new BrowseEvent(URI.create(homepage)));
        });
        versionLbl.setTooltip(new Tooltip("Visit homepage"));

        var footer = new HBox(versionLbl);
        footer.getStyleClass().add("footer");

        return footer;
    }

    // Forces the sidebar to focus on the navTree for keyboard navigation.
    void begForFocus() {
        navTree.requestFocus();
    }

    private void openSearchDialog() {
        var dialog = searchDialog.get();
        dialog.show(getScene());
        Platform.runLater(dialog::begForFocus);
    }

    private void openThemeDialog() {
        var dialog = themeDialog.get();
        dialog.show(getScene());
        Platform.runLater(dialog::requestFocus);
    }

    private class Header extends VBox {
        public Header() {
            super();

            getStyleClass().add("header");
            getChildren().setAll(createLogo(), createSearchButton());
        }

        private HBox createLogo() {
            var image = new ImageView(new Image(Resources.getResource("assets/app-icon.png").toString()));
            image.setFitWidth(32);
            image.setFitHeight(32);

            var imageBorder = new Insets(1);
            var imageBox    = new StackPane(image);
            imageBox.getStyleClass().add("image");
            imageBox.setPadding(imageBorder);
            imageBox.setPrefSize(image.getFitWidth() + imageBorder.getRight() * 2, image.getFitWidth() + imageBorder.getTop() * 2);
            imageBox.setMaxSize(image.getFitHeight() + imageBorder.getTop() * 2, image.getFitHeight() + imageBorder.getRight() * 2);

            var titleLbl = new Label("Enrollium");
            titleLbl.getStyleClass().addAll(TITLE_1);

            var themeSwitchBtn = new Button();
            themeSwitchBtn.getStyleClass().add("palette");
            themeSwitchBtn.setGraphic(new FontIcon(Material2MZ.SETTINGS));
            themeSwitchBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            themeSwitchBtn.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT);
            themeSwitchBtn.setAlignment(Pos.CENTER_RIGHT);
            themeSwitchBtn.setOnAction(_ -> openThemeDialog());

            var root = new HBox(10, imageBox, titleLbl, new Spacer(), themeSwitchBtn);
            root.getStyleClass().add("logo");
            root.setAlignment(Pos.CENTER_LEFT);

            return root;
        }

        private Button createSearchButton() {
            var titleLbl = new Label("Search", new FontIcon(Material2MZ.SEARCH));

            var hintLbl = new Label("Press /");
            hintLbl.getStyleClass().addAll("hint", TEXT_MUTED, TEXT_SMALL);

            var searchBox = new HBox(titleLbl, new Spacer(), hintLbl);
            searchBox.getStyleClass().add("content");
            searchBox.setAlignment(Pos.CENTER_LEFT);

            var root = new Button();
            root.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            root.getStyleClass().addAll("search-button");
            root.setGraphic(searchBox);
            root.setOnAction(e -> openSearchDialog());
            root.setMaxWidth(Double.MAX_VALUE);

            return root;
        }
    }
}
