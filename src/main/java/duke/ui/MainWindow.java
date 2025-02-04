package duke.ui;

import duke.Duke;
import duke.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
/**
 * Controller for MainWindow. Provides the layout for the other controls.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private UiController ui;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));
    private Image dukeImage = new Image(this.getClass().getResourceAsStream("/images/DaDuke.png"));

    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    public void setUiController(UiController ui) {
        this.ui = ui;
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing Duke's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        displayUserDialog(userInput.getText());
        displayDukeDialog(ui.getResponse(new Message(userInput.getText())).getMessage());
        userInput.clear();
    }

    /**
     * Creates a dialog entry originating from the user on the GUI.
     * @param userString <code>String</code> representing user message.
     */
    private void displayUserDialog(String userString) {
        dialogContainer.getChildren()
                .add(DialogBox.getUserDialog(userString, userImage));
    }

    /**
     * Creates a dialog entry originating from Duke on the GUI.
     * @param dukeString <code>String</code> representing Duke message.
     */
    void displayDukeDialog(String dukeString) {
        dialogContainer.getChildren()
                .add(DialogBox.getDukeDialog(dukeString, dukeImage));
    }
}