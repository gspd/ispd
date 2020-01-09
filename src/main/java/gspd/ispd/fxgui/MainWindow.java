package gspd.ispd.fxgui;

import gspd.ispd.ISPD;
import gspd.ispd.MainApp;
import gspd.ispd.model.User;
import gspd.ispd.model.VM;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    @FXML
    private TextArea terminalOutputArea;
    @FXML
    private TextField terminalInput;
    @FXML
    private TextArea helpArea;
    @FXML
    private Hyperlink machineIcon;
    @FXML
    private Hyperlink linkIcon;
    @FXML
    private Hyperlink clusterIcon;
    @FXML
    private Hyperlink switchIcon;
    @FXML
    private Hyperlink taskIcon;
    @FXML
    private Hyperlink dependencyIcon;
    @FXML
    private Hyperlink messageIcon;
    @FXML
    private Hyperlink synchronizationIcon;
    @FXML
    private Hyperlink hardwareMousePointerIcon;
    @FXML
    private Hyperlink workloadMousePointerIcon;
    @FXML
    private TableView<VM> vmTable;
    @FXML
    private TableColumn<VM, Integer> idVMColumn;
    @FXML
    private TableColumn<VM, String> userVMColumn;
    @FXML
    private TableColumn<VM, String> hypervisorVMColumn;
    @FXML
    private TableColumn<VM, Integer> coresVMColumn;
    @FXML
    private TableColumn<VM, Double> memoryVMColumn;
    @FXML
    private TableColumn<VM, Double> storageVMColumn;
    @FXML
    private TableColumn<VM, String> osVMColumn;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> idUserColumn;
    @FXML
    private TableColumn<User, String> nameUserColumn;
    @FXML
    private Button addUserButton;
    @FXML
    private Button removeUserButton;
    @FXML
    private Button addVMButton;
    @FXML
    private Button duplicateVMButton;
    @FXML
    private Button removeVMButton;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    private MenuItem simulateMenuItem;
    @FXML
    private Pane hardwarePane;
    @FXML
    private ScrollPane hardwareScrollPane;

    private MainApp main;
    private Stage window;

    public static void create(Stage window, MainApp main) {
        try {
            FXMLLoader loader;
            MainWindow controller;
            Scene scene;
            loader = main.getLoader();
            loader.setLocation(GUI.class.getResource("MainWindow.fxml"));
            scene = new Scene(loader.load());
            window.setScene(scene);
            controller = loader.getController();
            controller.setMain(main);
            controller.init();
            controller.setWindow(window);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        // disable remove user button [-] only if there is no selected item in user table
        removeUserButton.disableProperty().bind(userTable.getSelectionModel().selectedItemProperty().isNull());
        // disable remove VM button only if there is no selected item in the VM table
        removeVMButton.disableProperty().bind(vmTable.getSelectionModel().selectedItemProperty().isNull());
        // disable duplicate VM button only if there is no selected item in the VM table
        duplicateVMButton.disableProperty().bind(vmTable.getSelectionModel().selectedItemProperty().isNull());
        // USERS TABLE
        // the UID column of user table has the 'id' property of each user
        idUserColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        // the Name column of user table has the 'name' property of each user
        nameUserColumn.setCellValueFactory(row -> row.getValue().nameProperty());
        // any change in the user table changes directly the users list in the model
        userTable.setItems(main.getModel().getUsers());
        // it is not possible to add VM if there is no users
        addVMButton.disableProperty().bind(Bindings.isEmpty(userTable.getItems()));
        // every time an user row is double clicked, open an user dialog to change its data
        // to accomplish that, we have to set RowFactory
        userTable.setRowFactory(tableView -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // checks if click is doubled and is indeed a row with an user
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    User user = userTable.getSelectionModel().getSelectedItem();
                    int index = userTable.getSelectionModel().getSelectedIndex();
                    // open the user dialog to change the user
                    user = editUser(user);
                    // if the user was changed, change it in table
                    if (user != null) {
                        userTable.getItems().set(index, user);
                    }
                }
            });
            // its obligated to return the row, since we are defining a RowFactory
            return row;
        });
        // VMS TABLE
        idVMColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userVMColumn.setCellValueFactory(row -> row.getValue().getOwner().nameProperty());
        hypervisorVMColumn.setCellValueFactory(row -> row.getValue().hypervisorProperty());
        coresVMColumn.setCellValueFactory(new PropertyValueFactory<>("cores"));
        memoryVMColumn.setCellValueFactory(new PropertyValueFactory<>("memory"));
        storageVMColumn.setCellValueFactory(new PropertyValueFactory<>("storage"));
        osVMColumn.setCellValueFactory(row -> row.getValue().osProperty());
        vmTable.setItems(main.getModel().getVms());
        vmTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        vmTable.setRowFactory(tableView -> {
            TableRow<VM> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    VM vm = vmTable.getSelectionModel().getSelectedItem();
                    int index = vmTable.getSelectionModel().getSelectedIndex();
                    vm = editVM(vm);
                    if (vm != null) {
                        vmTable.getItems().set(index, vm);
                    }
                }
            });
            return row;
        });
        // Hardware Pane
        // the {width, height} of hardware pane (area that we draw hardware) is aways at least 1.5 times its parent scroll pane
        hardwarePane.minWidthProperty().bind(hardwareScrollPane.widthProperty().multiply(1.5));
        hardwarePane.minHeightProperty().bind(hardwareScrollPane.heightProperty().multiply(1.5));
        // the hardware scroll pane is pannable
        hardwareScrollPane.setPannable(true);
        // make machine icon hoverable (change opacity as user hovers)
        GUI.makeHoverable(machineIcon);
        // make link icon hoverable (change opacity as user hovers)
        GUI.makeHoverable(linkIcon);
        // make cluster icon hoverable (change opacity as user hovers)
        GUI.makeHoverable(clusterIcon);
        // make switch icon hoverable (change opacity as user hovers)
        GUI.makeHoverable(switchIcon);
    }

    public void setMain(MainApp main) {
        this.main = main;
    }

    @FXML
    private void handleEditSettings() {
        Stage settingsWindow = new Stage();
        SettingsWindow.create(window, settingsWindow, this.main);
    }

    public Stage getWindow() {
        return window;
    }

    public void setWindow(Stage window) {
        this.window = window;
    }

    @FXML
    private void handleCloseProgram() {
        main.close();
    }

    @FXML
    private void handleAddVmClicked() {
        // create a new window to insert VM
        VM vm = createVm();
        // if VM is returned
        if (vm != null) {
            // then adds in the model
            vmTable.getItems().add(vm);
        }
    }

    @FXML
    private void handleRemoveVmClicked() {
        vmTable.getItems().remove(vmTable.getSelectionModel().getSelectedIndex());
        vmTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleDuplicateVmClicked() {
        vmTable.getItems().add(new VM(vmTable.getSelectionModel().getSelectedItem()));
    }

    @FXML
    private void handleAddUserClicked() {
        User user = createUser();
        if (user != null) {
            userTable.getItems().add(user);
            user.setVms(vmTable.getItems().filtered(vm -> vm.getOwner().equals(user)));
        }
    }

    @FXML
    private void handleRemoveUser() {
        userTable.getItems().remove(userTable.getSelectionModel().getSelectedIndex());
        userTable.getSelectionModel().clearSelection();
    }

    private VM createVm() {
        VM vm = null;
        VMDialog controller;
        FXMLLoader loader;
        Scene scene;
        Stage dialog;
        Parent root;
        try {
            loader = new FXMLLoader();
            loader.setLocation(VMDialog.class.getResource("VMDialog.fxml"));
            loader.setResources(ISPD.getStrings());
            root = loader.load();
            scene = new Scene(root);
            dialog = new Stage();
            controller = loader.getController();
            controller.setWindow(dialog);
            controller.setMain(main);
            controller.init();
            dialog.initOwner(window);
            dialog.setTitle("Edit VM");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(scene);
            dialog.showAndWait();
            vm = controller.getVm();
        } catch (IOException e) {
            System.out.println("Error loading FXML");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return vm;
    }

    private VM editVM(VM vm) {
        VMDialog controller;
        FXMLLoader loader;
        Scene scene;
        Stage dialog;
        Parent root;
        try {
            loader = new FXMLLoader();
            loader.setLocation(VMDialog.class.getResource("VMDialog.fxml"));
            loader.setResources(ISPD.getStrings());
            root = loader.load();
            scene = new Scene(root);
            dialog = new Stage();
            controller = loader.getController();
            controller.setWindow(dialog);
            controller.setMain(main);
            controller.init();
            controller.loadVM(vm);
            dialog.initOwner(window);
            dialog.setTitle("Edit VM");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(scene);
            dialog.showAndWait();
            vm = controller.getVm();
        } catch (IOException e) {
            System.out.println("Error loading FXML");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return vm;

    }

    private User createUser() {
        User user = null;
        FXMLLoader loader;
        Scene scene;
        Stage dialog;
        UserDialog controller;
        Parent root;
        try {
            loader = new FXMLLoader();
            loader.setLocation(UserDialog.class.getResource("UserDialog.fxml"));
            loader.setResources(ISPD.getStrings());
            root = loader.load();
            scene = new Scene(root);
            dialog = new Stage();
            controller = loader.getController();
            controller.setWindow(dialog);
            controller.setMain(main);
            controller.init();
            dialog.initOwner(window);
            dialog.setTitle("New User");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(scene);
            dialog.showAndWait();
            user = controller.getUser();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    private User editUser(User user) {
        FXMLLoader loader;
        Scene scene;
        Stage dialog;
        UserDialog controller;
        Parent root;
        try {
            loader = new FXMLLoader();
            loader.setLocation(UserDialog.class.getResource("UserDialog.fxml"));
            loader.setResources(ISPD.getStrings());
            root = loader.load();
            scene = new Scene(root);
            dialog = new Stage();
            controller = loader.getController();
            controller.setWindow(dialog);
            controller.setMain(main);
            controller.init();
            controller.loadUser(user);
            dialog.initOwner(window);
            dialog.setTitle("Edit User");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(scene);
            dialog.showAndWait();
            user = controller.getUser();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
