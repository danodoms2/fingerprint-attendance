/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package Controller;

import Utilities.ImageUtil;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class FP_IdentificationSuccessCTRL implements Initializable {

    @FXML
    private Label nameLabel;
    @FXML
    private Label nameLabel1;
    @FXML
    private ImageView userImageView;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    
     public void setUserData(String userName, byte[] imageData) {
        // Set the actual image and name in the placeholder
        userImageView.setImage(ImageUtil.byteArrayToImage(imageData));
        nameLabel.setText(userName);
    }
}