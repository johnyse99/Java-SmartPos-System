package io.smartpos.ui.catalog;

import io.smartpos.core.domain.customer.Customer;
import io.smartpos.core.domain.product.Category;
import io.smartpos.core.domain.purchase.Supplier;
import io.smartpos.core.domain.user.User;
import io.smartpos.infrastructure.dao.CategoryDao;
import io.smartpos.infrastructure.dao.CustomerDao;
import io.smartpos.infrastructure.dao.SupplierDao;
import io.smartpos.infrastructure.dao.UserDao;
import io.smartpos.ui.config.ServiceFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CatalogController {

    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, Integer> colCatId;
    @FXML
    private TableColumn<Category, String> colCatName;
    @FXML
    private TableColumn<Category, Boolean> colCatStatus;

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, Integer> colCustId;
    @FXML
    private TableColumn<Customer, String> colCustName;
    @FXML
    private TableColumn<Customer, String> colCustDoc;
    @FXML
    private TableColumn<Customer, String> colCustPhone;
    @FXML
    private TableColumn<Customer, String> colCustEmail;

    @FXML
    private TableView<Supplier> supplierTable;
    @FXML
    private TableColumn<Supplier, Integer> colSuppId;
    @FXML
    private TableColumn<Supplier, String> colSuppName;
    @FXML
    private TableColumn<Supplier, String> colSuppDoc;
    @FXML
    private TableColumn<Supplier, String> colSuppPhone;

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> colUserId;
    @FXML
    private TableColumn<User, String> colUserUsername;
    @FXML
    private TableColumn<User, String> colUserRole;
    @FXML
    private TableColumn<User, Boolean> colUserStatus;

    private final CategoryDao categoryDao;
    private final CustomerDao customerDao;
    private final SupplierDao supplierDao;
    private final UserDao userDao;

    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    public CatalogController() {
        this.categoryDao = ServiceFactory.categoryDao();
        this.customerDao = ServiceFactory.customerDao();
        this.supplierDao = ServiceFactory.supplierDao();
        this.userDao = ServiceFactory.userDao();
    }

    @FXML
    public void initialize() {
        setupCategoryTable();
        setupCustomerTable();
        setupSupplierTable();
        setupUserTable();

        loadAll();
    }

    private void setupCategoryTable() {
        colCatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCatName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCatStatus.setCellValueFactory(new PropertyValueFactory<>("active"));

        TableColumn<Category, String> colImage = new TableColumn<>("Image");
        colImage.setPrefWidth(100);
        colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        colImage.setCellFactory(param -> new TableCell<Category, String>() {
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null || url.isEmpty()) {
                    setGraphic(new Label("🖼️"));
                } else {
                    try {
                        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(
                                new javafx.scene.image.Image(url, 32, 32, true, true));
                        setGraphic(iv);
                    } catch (Exception e) {
                        setGraphic(new Label("❌Img"));
                    }
                }
            }
        });
        categoryTable.getColumns().add(colImage);

        categoryTable.setItems(categoryList);
    }

    private void setupCustomerTable() {
        colCustId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCustDoc.setCellValueFactory(new PropertyValueFactory<>("document"));
        colCustPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCustEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        customerTable.setItems(customerList);
    }

    private void setupSupplierTable() {
        colSuppId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSuppName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSuppDoc.setCellValueFactory(new PropertyValueFactory<>("document"));
        colSuppPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        supplierTable.setItems(supplierList);
    }

    private void setupUserTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("active"));
        userTable.setItems(userList);
    }

    private void loadAll() {
        categoryList.setAll(categoryDao.findAllActive());
        customerList.setAll(customerDao.findAllActive());
        supplierList.setAll(supplierDao.findAllActive());
        userList.setAll(userDao.findAllActive());
    }

    @FXML
    private void handleAddCategory() {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("New Category");
        dialog.setHeaderText("Create a new product category");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Category Name");
        TextField urlField = new TextField();
        urlField.setPromptText("Image URL (optional)");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Image URL:"), 0, 1);
        grid.add(urlField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Category cat = new Category();
                cat.setName(nameField.getText());
                cat.setImageUrl(urlField.getText());
                cat.setActive(true);
                return cat;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cat -> {
            if (cat.getName() != null && !cat.getName().trim().isEmpty()) {
                categoryDao.save(cat);
                loadAll();
            }
        });
    }

    @FXML
    private void handleAddCustomer() {
        // Placeholder or simple dialog
        showAlert("Coming Soon", "Customer management form will be implemented in the next module.");
    }

    @FXML
    private void handleAddSupplier() {
        showAlert("Coming Soon", "Supplier management form will be implemented in the next module.");
    }

    @FXML
    private void handleAddUser() {
        showAlert("Coming Soon", "User management form will be implemented in the next module.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
