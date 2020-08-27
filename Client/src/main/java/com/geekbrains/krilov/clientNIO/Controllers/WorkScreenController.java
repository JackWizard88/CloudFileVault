package com.geekbrains.krilov.clientNIO.Controllers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import com.geekbrains.krilov.FileInfo;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class WorkScreenController {

    private Path root = Paths.get(".");

    @FXML
    private URL location;

    @FXML
    private TableView localTable;

    @FXML
    private TableView serverTable;

    @FXML
    private TextField localPathFiled;

    @FXML
    private TextField serverPathFiled;

    @FXML
    private MenuItem menuItemExit;

    @FXML
    private Button btnCopyToServer;

    @FXML
    private Button btnCopyFromServer;

    @FXML
    private Button btnDel;

    @FXML
    void initialize() {

        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

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
        updateLocalList(root);
        updateServerList(root);
    }

    private void updateServerList(Path path) {
        ClientController.getInstance().getNetworkService().getServerFileList(path);
    }

    public void updateLocalList(Path path) {
        try {
            localPathFiled.setText(path.normalize().toAbsolutePath().toString());
            localTable.getItems().clear();
            localTable.getItems().addAll(Files.list(path).filter(Files::isReadable).map(FileInfo::new).collect(Collectors.toList()));
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

    private void deleteFile(Path path) {
        //todo
    }

}

