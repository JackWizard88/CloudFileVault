package com.geekbrains.krilov.clientNIO.Controllers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import com.geekbrains.krilov.FileInfo;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class WorkScreenController extends BaseController {

    private Path root = Paths.get(".");
    private Path currentClientPath = root;
    private Path currentServerPath;

    @FXML
    private URL location;

    @FXML
    private TableView<FileInfo> localTable;

    @FXML
    private TableView<FileInfo> serverTable;

    @FXML
    private TextField localPathField;

    @FXML
    private TextField serverPathField;

    @FXML
    private MenuItem menuItemExit;

    @FXML
    private Button btnCopyToServer;

    @FXML
    private Button btnLocalUp;

    @FXML
    private Button btnServerUp;

    @FXML
    private Button btnCopyFromServer;

    @FXML
    private Button btnDel;

    @FXML
    void initialize() {

        btnServerUp.setOnAction(e -> serverUp(e));

        localTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 & getSelectedFilename() != null) {
                Path path = Paths.get(localPathField.getText()).resolve(localTable.getSelectionModel().getSelectedItem().getFilename());
                if (Files.isDirectory(path)) {
                    updateLocalList(path);
                }
            }
        });

        serverTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 & getSelectedFilename() != null) {
                Path path = Paths.get(serverPathField.getText()).resolve(serverTable.getSelectionModel().getSelectedItem().getFilename());
                if (Files.isDirectory(path)) {
                    updateServerList(path);
                }
            }
        });

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });
        fileSizeColumn.setPrefWidth(120);

        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified()));
        fileDateColumn.setPrefWidth(120);

        localTable.getColumns().addAll(fileNameColumn, fileSizeColumn, fileDateColumn);
        localTable.getSortOrder().add(fileSizeColumn);


        TableColumn<FileInfo, String> fileNameColumn1 = new TableColumn<>("Имя");
        fileNameColumn1.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn1.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn1 = new TableColumn<>("Размер");
        fileSizeColumn1.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn1.setCellFactory(column -> new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        });
        fileSizeColumn.setPrefWidth(120);

        TableColumn<FileInfo, String> fileDateColumn1 = new TableColumn<>("Дата изменения");
        fileDateColumn1.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified()));
        fileDateColumn1.setPrefWidth(120);

        serverTable.getColumns().addAll(fileNameColumn1, fileSizeColumn1, fileDateColumn1);
    }

    public void update() {
        new Thread(() -> {
            try {
                updateLocalList(currentClientPath);
                currentServerPath = Paths.get(ClientController.getInstance().getServerRootPath());
                serverPathField.setText(currentServerPath.toString());
                updateServerList(currentServerPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateServerList(Path path) {

        try {
            List<FileInfo> list = ClientController.getInstance().getServerFileList(path);
            if (list == null) {
                ScreenController.getInstance().showErrorMessage("не удалось обновить список файлов", null);
                return;
            }
            serverTable.getItems().clear();
            serverTable.getItems().addAll(list);
            serverTable.sort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateLocalList(Path path) {
        try {
            currentClientPath = path;
            localPathField.setText(currentClientPath.normalize().toAbsolutePath().toString());
            localTable.getItems().clear();
            localTable.getItems().addAll(Files.list(currentClientPath).filter(Files::isReadable).map(FileInfo::new).collect(Collectors.toList()));
            localTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void sendFile(Path path) {
        //todo
    }

    private void getFile(Path path) {
        //todo
    }

    public void localUp(ActionEvent actionEvent) {
        Path upperPath = Paths.get(localPathField.getText()).getParent();
        if (upperPath != null) {
            updateLocalList(upperPath);
        }
    }

    public void serverUp(ActionEvent actionEvent) {
        Path upperPath = Paths.get(serverPathField.getText()).getParent();
        if (upperPath != null) {
            updateServerList(upperPath);
        }

    }

    public String getSelectedFilename() {
        String fileName = null;
        if (localTable.isFocused()) {
            if (localTable.getSelectionModel().getSelectedItem() != null) {
                fileName = localTable.getSelectionModel().getSelectedItem().getFilename();
            }
        } else if (serverTable.isFocused()) {
            if (serverTable.getSelectionModel().getSelectedItem() != null) {
                fileName = serverTable.getSelectionModel().getSelectedItem().getFilename();
            }
        }

        return fileName;
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void deleteFile(ActionEvent actionEvent) {
        Path path;
        if  (serverTable.isFocused()) {
            path = Paths.get(currentServerPath.toString() + "/"+ getSelectedFilename());
            ClientController.getInstance().deleteFile(path, () -> {
                ScreenController.getInstance().showInfoMessage("Файл удален");
                updateServerList(currentServerPath);
            });
        } else if (localTable.isFocused()) {
            path = Paths.get(currentClientPath.toString() + "/"+ getSelectedFilename());
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                updateLocalList(currentClientPath);
            }
        }
    }
}

