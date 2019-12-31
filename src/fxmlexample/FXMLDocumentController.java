/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxmlexample;

import javafx.scene.control.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;



/**
 *
 * @author ak
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML Text filename;
    @FXML TextField txt_btchsize;
    @FXML TextArea appout;
    @FXML TextField txt_recCount;
    @FXML TextField txt_retPeriod;
    @FXML ChoiceBox condition;
    @FXML ChoiceBox ndelr;
    @FXML ChoiceBox ndelm;
    @FXML TextField user;
    @FXML TextField pass;
    @FXML TextField txt_trackno;
    @FXML RadioButton f1;
    @FXML RadioButton f2;
    boolean secondphase = false;
    
    private String tracknos;
    List<String> regnos = new ArrayList<String>();
    List<String> buffer = new ArrayList<String>();
    
    @FXML
    private void filePickerClicked(ActionEvent event) {
    JFileChooser fileChooser = new JFileChooser();
    FileNameExtensionFilter extFilter = new FileNameExtensionFilter( "Excel file","xls");
    fileChooser.setFileFilter(extFilter);
    fileChooser.showOpenDialog(null);
    java.io.File file = fileChooser.getSelectedFile();
    if(file!=null){
    filename.setText(file.getAbsolutePath());
    tracknos = file.getAbsolutePath();
    }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }  
    @FXML
    private void onEnter(ActionEvent event)
    {
    appout.appendText(txt_trackno.getText()+"\n");
    regnos.add(txt_trackno.getText());
    buffer.add(txt_trackno.getText());
    txt_trackno.clear();
    txt_recCount.setText(String.valueOf(regnos.size()));
    }
    @FXML
    public void resetClicked(ActionEvent event)
    {
    appout.clear();
    txt_recCount.clear();
    if(regnos.size()>0)regnos.clear();
    }
    @FXML 
    public void reloadClicked(ActionEvent event)
    {
     regnos.clear();
     if(!buffer.isEmpty()) 
     {
     for(String value:buffer)
     {
         appout.appendText(value);
         regnos.add(value);
     }
     }
    }
    @FXML 
    public void recBufClicked(ActionEvent event)
    {
     if(!buffer.isEmpty()) buffer.clear();
    }
    @FXML
    private void processClicked(ActionEvent event)
    {
     appout.clear();
     if(txt_recCount.getText().isEmpty())
     {
     JOptionPane.showMessageDialog(null, "Please specify a record count");
     return;
     }
     if(user.getText().isEmpty())
     {
     JOptionPane.showMessageDialog(null, "Please specify Username");
     return;
     }
     if(pass.getText().isEmpty())
     {
     JOptionPane.showMessageDialog(null, "Please input password");
     return;
     }
      if(tracknos == null && f1.isSelected())
     {
     JOptionPane.showMessageDialog(null, "Please select a file");
     return;
     }
       if(tracknos != null && !f1.isSelected())
     {
     JOptionPane.showMessageDialog(null, "Please click the load excel file radiobutton");
     return;
     
     }
     
      /* if(!isConnected()) 
       {
           appout.appendText("Internet Connectivity Broken");
           return;
       }*/
     int count = Integer.parseInt(txt_recCount.getText());
     //new HtmlBrowser().processHandler(tracknos, Integer.parseInt(txt_btchsize.getText()),count);
     HtmlBrowser browser = new HtmlBrowser();
     browser.file=tracknos;
     if(f2.isSelected()) browser.udaonly = true;
     browser.batchSize=Integer.parseInt(txt_btchsize.getText());
     browser.recCount = count;
     browser.tarea = appout;
     browser.username = user.getText();
     browser.passw = pass.getText();
     browser.retrialCycle = Integer.parseInt(txt_retPeriod.getText());
     browser.condition = condition.getValue().toString();
     browser.nondeliver_m = ndelm.getSelectionModel().getSelectedIndex();
     browser.nondeliver_r = ndelr.getSelectionModel().getSelectedIndex();
     if(f1.isSelected()) browser.useFile = true;
     if(!f1.isSelected() && regnos.isEmpty())
     {
      return;
     }
      if(!f1.isSelected() && regnos.size()>0)
     {
      browser.tracknos = regnos;
     }
     appout.appendText("Processing.................\n");
     browser.startTask();
    }
    
    
}
