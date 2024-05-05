package g3.scms.controllers;

import org.kordamp.bootstrapfx.BootstrapFX;

import g3.scms.api.RestApi;
import g3.scms.model.AdmissionType;
import g3.scms.model.College;
import g3.scms.model.Degree;
import g3.scms.model.Department;
import g3.scms.model.Message;
import g3.scms.model.Request;
import g3.scms.model.SecurePassword;
import g3.scms.model.Student;
import g3.scms.utils.ReqRes;
import g3.scms.utils.Util;
import g3.scms.utils.Validate;
import g3.scms.utils.Views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;

public class SignupController {
  @FXML private AnchorPane signupAnchorPane;
  @FXML private TextField classYear;
  @FXML private TextField emailAddress;
  @FXML private TextField firstName;
  @FXML private TextField idNumber;
  @FXML private TextField lastName;
  @FXML private TextField middleName;
  @FXML private PasswordField password;
  @FXML private TextField section;
  
  @FXML private MenuButton department;
  @FXML private MenuButton admissionType;
  @FXML private MenuButton college;
  @FXML private MenuButton degree;
  
  @FXML private Label admissionTypeError;
  @FXML private Label classYearError;
  @FXML private Label collegeError;
  @FXML private Label degreeError;
  @FXML private Label departmentError;
  @FXML private Label emailError;
  @FXML private Label firstNameError;
  @FXML private Label idNumberError;
  @FXML private Label lastNameError;
  @FXML private Label middleNameError;
  @FXML private Label passwordError;
  @FXML private Label sectionError;
  
  @FXML void handleSelectedAdmissionType(ActionEvent event) {
    selectedAdmissionType = (RadioMenuItem) event.getSource();
    admissionType.setText(selectedAdmissionType.getText());
  }

  @FXML void handleSelectedCollege(ActionEvent event) {
    selectedCollege = (RadioMenuItem) event.getSource();
    college.setText(selectedCollege.getText());
  }

  @FXML void handleSelectedDegree(ActionEvent event) {
    selectedDegree = (RadioMenuItem) event.getSource();
    degree.setText(selectedDegree.getText());
  }

  @FXML void handleSelectedDepartment(ActionEvent event) {
    selectedDepartment = (RadioMenuItem) event.getSource();
    department.setText(selectedDepartment.getText());
  }
  
  private static RadioMenuItem selectedCollege = null;
  private static RadioMenuItem selectedDepartment = null;
  private static RadioMenuItem selectedDegree = null;
  private static RadioMenuItem selectedAdmissionType = null;

  private void setErrorMessage(String message, int errCode) {
    switch (errCode) {
      case 1: firstNameError.setText(message); break;
      case 2: middleNameError.setText(message); break;
      case 3: lastNameError.setText(message); break;
      case 4: emailError.setText(message); break;
      case 5: idNumberError.setText(message); break;
      case 6: classYearError.setText(message); break;
      case 7: sectionError.setText(message); break;
      case 8: passwordError.setText(message); break;
      case 9: collegeError.setText(message); break;
      case 10: departmentError.setText(message); break;
      case 11: degreeError.setText(message); break;
      case 12: admissionTypeError.setText(message); break;
      default:
        break;
    }
  }
  
  private void remove(Label label) {
    label.setText("");
  }
  
  @FXML void handleBackToLoginBtn(ActionEvent event) {
    try {
        AnchorPane loginPane = (AnchorPane) Views.loadFXML("/views/login_page");
        loginPane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        AnchorPane inputFieldAnchorPane = (AnchorPane) signupAnchorPane.getParent();

        Views.paintPage(loginPane, inputFieldAnchorPane, 0, 0, 0, 0);
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }
  }

  @FXML
  void handleProceedBtn(ActionEvent event) {
    // Lets collect validate the grabbed data
    var _fname = firstName.getText();
    var _mname = middleName.getText();
    var _lname = lastName.getText();
    var _email = emailAddress.getText();
    var _id = idNumber.getText();
    var _class_year = classYear.getText();
    var _section = section.getText();
    var _college = selectedCollege != null ? selectedCollege.getText() : null;
    var _deprtment = selectedDepartment != null ? selectedDepartment.getText() : null;
    var _degree = selectedDegree != null ? selectedDegree.getText() : null;
    var _admission = selectedAdmissionType != null ? selectedAdmissionType.getText() : null;
    var _pwd = password.getText();

    // Validation
    var errCode = 1;
    try {
      Validate.name(_fname); remove(firstNameError); ++errCode;
      Validate.name(_mname); remove(middleNameError);  ++errCode;
      Validate.name(_lname); remove(lastNameError);  ++errCode;
      Validate.email(_email); remove(emailError);  ++errCode;
      Validate.idNumber(_id); remove(idNumberError); ++errCode;
      Validate.classYear(_class_year); remove(classYearError); ++errCode;
      Validate.section(_section); remove(sectionError); ++errCode;
      Validate.password(_pwd); remove(passwordError); ++errCode;
      if(_college == null) throw new Error("Pick a college"); remove(collegeError); ++errCode;
      if(_deprtment == null) throw new Error("Pick a deprtment"); remove(departmentError); ++errCode;
      if(_degree == null) throw new Error("Pick a degree"); remove(degreeError); ++errCode;
      if(_admission == null) throw new Error("Pick an admission type"); remove(admissionTypeError); ++errCode;
    } catch (Error e) {
      setErrorMessage(e.getMessage(), errCode);
      return;
    }

    // Since now we are past the validation let's begin sending the request.
    // First create the json from the Student object/model
    Student student = new Student(_fname, _mname, _lname, _id, _email, _section, Integer.parseInt(_class_year), College.toEnum(_college),
        Department.toEnum(_deprtment), Degree.toEnum(_degree), AdmissionType.toEnum(_admission));
    String json = ReqRes.makeJsonString(student);
    
    // Lets encrypt the user input password to put in the header request to prevent cross site scripting
    String salt = Util.generateSalt(16);
    SecurePassword securePassword = SecurePassword.saltAndHashPassword(salt, _pwd);
    String passwordJson = ReqRes.makeJsonString(securePassword);

    // Send the user data to the server
    // Build the request
    Request request = new Request();
    request.setBaseUrl(g3.scms.utils.Util.getEnv().getProperty("API_BASE_URL"));
    request.setPath("api/auth/signup");
    request.setJsonBody(json);
    request.setHeaderMap("Password", passwordJson);
    
    // Here since the user is new we have no auth token to put in header
    RestApi api = new RestApi();
    api.post(request, (err, resp) -> {
      // Something is wrong with the user connection or the server
      if (err != null) {
        Views.displayAlert(AlertType.WARNING, "Connection/Server Error", "The thing is", err.getMessage());
        return null;
      }

      int responseCode = resp.statusCode();
      var responseMessage = (Message) ReqRes.makeModelFromJson(resp.body(), Message.class);

      // The user requested a bad one.
      if (responseCode != 201) {
        Views.displayAlert(AlertType.ERROR, "Bad Request", "The username already exists", responseMessage.getMessage());
        return null;
      }

      // Seems succesSful operation
      System.out.println(responseMessage.getMessage());
      return resp;
    });
  }
}
