
package fxmlexample;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javafx.application.Platform;
import javafx.scene.control.*;





public class HtmlBrowser {
	public List<String> tracknos = new ArrayList<String>();
        private HtmlPage page;
        String file;
        int batchSize;
        int recCount;
        int retrialCycle;
        String username;
        String passw;
        String condition;
        int nondeliver_r;
        int nondeliver_m;
        javafx.scene.control.TextArea tarea;
        boolean useFile = false;
        boolean reachable = true;
        boolean udaonly = false;
        int lastStore =0;
        BufferedWriter writer;
	

	public static void main(String[] args) 
	{
            
		

	}
	protected String addItem(String entry)
	{
		try
		{
                
                 
                HtmlTextInput identifier = (HtmlTextInput)page.getElementById("MailitemIdentifier");
		identifier.setValueAttribute(entry);
                //identifier.getValueAttribute();
		HtmlButton addbtn = (HtmlButton)page.getElementById("btnAddOrStore");
		page = addbtn.click();
                int numtries = 0;
                 while(page.getWebClient().waitForBackgroundJavaScript(5000) > 0)
                 {
                  
                  if(numtries == 5) return "bad_wait";
                  numtries++;
                 }
                 //Thread.sleep(5000);
                //}
                 }
		
		catch(Exception ex)
		{
	     ex.printStackTrace();
		}
                return page.asText();
	}
	public void processHandler()
	{
		int btchsz = batchSize;
                int batchmark = 0;
                try 
                
		{
		 writer = new BufferedWriter(new FileWriter("process.log"));
                 if(!isConnected())
                 {
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("Local Area Connectivity Broken");
                 }
                 });
                 reachable = false;
                 return;
                 }
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("Establishing connection to IPS Servers......\n");
                 }
                 });
                WebClient client = new WebClient();
                client.getOptions().setTimeout(120000);
                client.getOptions().setJavaScriptEnabled(true);
		page = client.getPage("http://41.204.247.247/IPSWeb/");
		HtmlInput user = page.getElementByName("Username");
		user.setValueAttribute(username);
		HtmlInput pass = page.getElementByName("Password");
		pass.setValueAttribute(passw);
		HtmlButton btn =  (HtmlButton) page.getElementById("btnLogin");
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("Loggin in.......\n");
                 }
                 });
		page = btn.click();
                if(page.asText().contains("Incorrect username or password"))
                {
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("Incorrect username or password\n");
                 }
                 });
                 reachable = false;
                 return;
                }
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("Logged In\n");
                 }
                 });
		HtmlAnchor tracktrace = page.getAnchorByText("Letters");
		page = tracktrace.click();
		HtmlAnchor loc_office_rec = page.getAnchorByText("Receive letters at local delivery office (EMG)");
		page = loc_office_rec.click();
                if(useFile)
                {
                ExcelReader xReader =  new ExcelReader();
		tracknos = xReader.readFromExcel(file,recCount);
                }
                if(udaonly) return;
                Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("Setting Capture parameters\n");
                 }
                 });
		HtmlCheckBoxInput conditionPin = (HtmlCheckBoxInput)page.getElementByName("ConditionPinCheckBox");
		page =  conditionPin.click();
		HtmlSelect condition  =  page.getElementByName("Condition");
                condition.setSelectedIndex(1);
                
                //Thread.sleep(2000);
                int count = 0;
		while(count < recCount)
                {
		 
                 String value = tracknos.get(count);
                 String response = addItem(value);
                 System.out.println(response);
                 writer.append(response);
                 HtmlTextInput identifier = (HtmlTextInput)page.getElementById("MailitemIdentifier");
                 if(!identifier.getValueAttribute().equals(value) && response.contains(value)&& page.getElementsByTagName("table").size()>0)
                 {
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText(value+ " added\n");
                 }
                 });
                 }
                 if(response.contains("An error occurred while processing the request"))
                 {
                 //batchmark = count;
                 count=lastStore;
                 btchsz = count + batchSize;
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("ERROR ADDING ITEM....RETRYING UNSAVED BATCH......IN"+(retrialCycle/1000)+" SECONDS\n");
                 }
                 });
                 Thread.sleep(retrialCycle);
                 continue;
                 }
                 if(response.contains("Item not found in the database"))
                 {
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("item not found in database \n");
                 }
                 });
                 
                 }
                 
                 Thread.sleep(500);
                 count++;
                 if(count==btchsz || count == recCount )
                 {
                    //  batchmark = count;
                     String result = storeitems();
                     if(result.contains("error occurred while")) 
                     {
                          count = lastStore;
                         //batchmark = count;
                         //btchsz = count + batchSize;
                         Platform.runLater(new Runnable(){
                         public void run()
                         {
                       tarea.appendText("ERROR STORING BATCH.....RETRYING UNSAVED BATCH......IN"+(retrialCycle/1000)+" SECONDS\n");
                         }
                        });
                         Thread.sleep(retrialCycle);
                         continue;
                     }
                     if(result.contains("The operation was successful"))
                     {  
                         lastStore = count;
                         btchsz=batchSize+count;
                         Platform.runLater(new Runnable(){
                         public void run()
                         {
                         tarea.appendText("Store successful..commencing next batch!!\n");
                         }
                         });
                     
                     }
                     
                     if(result.equals(""))
                     {
                         btchsz=batchSize+count;
                         Platform.runLater(new Runnable(){
                         public void run()
                         {
                         tarea.appendText("Could not commit batch...\n");
                         }
                         });
                     }
                     else 
                         btchsz = batchSize + count;
                 
                 }
                  
                
                
                }
                }
		catch(Exception ex)
		{
		reachable = false;
                System.out.println(ex.getMessage());
                JOptionPane.showMessageDialog(null,"Service is unreachable");
                Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.clear();
                 }
                 });
                
                   
		}
			
	}
        protected void unsuccessful()
        {
        Platform.runLater(new Runnable(){
                public void run()
                {
                tarea.appendText("REGISTERING FOR UNSUCCESSFUL DELIVERY........\n");
                }
               });
        if(!reachable) return;
        int btchsz = batchSize;
        lastStore = 0;
        try
        {
        writer.append("REGISTERING FOR UNSUCCESSFUL PHASE..");
        HtmlAnchor foInb = page.getAnchorByText("Inbound");
        page = foInb.click();
        HtmlAnchor unsuc = page.getAnchorByText("Record unsuccessful delivery (EMH)");
        page = unsuc.click();
        HtmlCheckBoxInput conditionPin = (HtmlCheckBoxInput)page.getElementByName("NonDeliveryReasonPinCheckBox");
	conditionPin.click();
	HtmlSelect condition  =  page.getElementByName("NonDeliveryReason");
        if(nondeliver_r==0)
        condition.setSelectedIndex(21);
        if(nondeliver_r==1)
        condition.setSelectedIndex(20);
        HtmlCheckBoxInput nondelmeas = (HtmlCheckBoxInput)page.getElementByName("NonDeliveryMeasurePinCheckBox");
        nondelmeas.click();
        HtmlSelect nondelmeasact = (HtmlSelect)page.getElementById("NonDeliveryMeasure");
        if(nondeliver_m==0)
        {
        page.executeJavaScript(JScripts.code1);
        HtmlOption option = nondelmeasact.getOptionByValue("B");   
        nondelmeasact.setSelectedAttribute(option, true);
        }
        if(nondeliver_m==1)
        {
        page.executeJavaScript(JScripts.code1);
        HtmlOption option = nondelmeasact.getOptionByValue("F");   
        nondelmeasact.setSelectedAttribute(option, true);
        }
        int count = 0;
		while(count < recCount)
                {
		 
                 String value = tracknos.get(count);
                 String response = addItem(value);
                 System.out.println(response);
                 writer.append(response);
                 HtmlTextInput identifier = (HtmlTextInput)page.getElementById("MailitemIdentifier");
                 if(!identifier.getValueAttribute().equals(value)&&response.contains(value) && page.getElementsByTagName("table").size()>0)
                 {
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText(value+ " added\n");
                 }
                 });
                 }
                 if(response.contains("An error occurred while processing the request"))
                 {
                 //batchmark = count;
                 count=btchsz-batchSize;
                 btchsz = count + batchSize;
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("ERROR ADDING ITEM....RETRYING UNSAVED BATCH......IN"+(retrialCycle/1000)+" SECONDS\n");
                 }
                 });
                 Thread.sleep(retrialCycle);
                 continue;
                 }
                 if(response.contains("Item not found in the database"))
                 {
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("item not found in database \n");
                 }
                 });
                 }
                 if(response.contains("The Non delivery measure field is required.")||response.contains("unchecked Non delivery measure *"))
                 {
                 nondelmeas.setChecked(true);
                 if(nondeliver_m==0)
                 {
                 page.executeJavaScript(JScripts.code1);
                 HtmlOption option = nondelmeasact.getOptionByValue("B");   
                 nondelmeasact.setSelectedAttribute(option, true);
                 }
                 if(nondeliver_m==1)
                {
                 page.executeJavaScript(JScripts.code1);
                 HtmlOption option = nondelmeasact.getOptionByValue("F");   
                 nondelmeasact.setSelectedAttribute(option, true);
                 }
                 if(identifier.getValueAttribute().equals(value))
                 continue;
                 }
                /* else 
                 {
                 Platform.runLater(new Runnable(){
                 public void run()
                 {
                 tarea.appendText("could not add "+value+"\n");
                 }
                 });
                 
                 }*/
                 Thread.sleep(500);
                 count++;
                 if(count==btchsz || count == recCount)
                 {
                     String result = storeitems();
                     if(result.contains("error occurred while")) 
                     {
                         count=lastStore;
                         //btchsz = count + batchSize;
                         Platform.runLater(new Runnable(){
                         public void run()
                       {
                       tarea.appendText("ERROR ADDING ITEM....RETRYING UNSAVED BATCH......IN"+(retrialCycle/1000)+" SECONDS\n");
                       }
                        });
                         Thread.sleep(retrialCycle);
                     }
                     if(result.contains("The operation was successful"))
                     {  
                         lastStore = count;
                         btchsz=batchSize+count;
                         Platform.runLater(new Runnable(){
                         public void run()
                         {
                         tarea.appendText("Store successful..commencing next batch!!\n");
                         }
                         });
                     
                     }
                     if(result.equals(""))
                     {
                         btchsz=batchSize+count;
                         Platform.runLater(new Runnable(){
                         public void run()
                         {
                         tarea.appendText("Could not commit batch...\n");
                         }
                         });
                     }
                     else 
                         btchsz = batchSize + count;
                 
                 }
                  
                
                
                }
		JOptionPane.showMessageDialog(null, "Processing Complete");
                 writer.close();
		
        
        }
        catch(IndexOutOfBoundsException ex)
        {
        JOptionPane.showMessageDialog(null, "Cannot select from empty options");
        Platform.runLater(new Runnable(){
                         public void run()
                         {
                         tarea.clear();
                         }
                         });
        }
        catch(Exception ex)
        {
        JOptionPane.showMessageDialog(null, ex.getMessage());
        Platform.runLater(new Runnable(){
                         public void run()
                         {
                         tarea.clear();
                         }
                         });
        }
        }
        public void  startTask()
        {
        
        Runnable task = new Runnable(){
        @Override
        public void run()
        {
        processHandler();
        unsuccessful();
        }
        };
        Thread worker = new Thread(task);
        worker.setDaemon(true);
        worker.start();
        
        
        }
        protected String storeitems()
        {
              String pagehtml = "";
            try
		{
               
                 
		HtmlButton addbtn = (HtmlButton)page.getElementById("btnStore");
                if(addbtn!=null)
                {
		page = addbtn.click();
                
                //Thread.sleep(10000);
                int numtries = 0;
                 while(page.getWebClient().waitForBackgroundJavaScript(60000) > 0)
                 {
                  
                  if(numtries == 5) return "bad_wait";
                  numtries++;
                 }
                pagehtml = page.asText();
                }
               
                
                
		}
		catch(Exception ex)
		{
	         //ex.printStackTrace();
             
		}
            return pagehtml;
        }
        public boolean isConnected()
    {
    boolean connected = false;
    try {
         URL url = new URL("http://192.168.8.1");
         URLConnection connection = url.openConnection();
         connection.connect();
         connected = true;
      } catch (MalformedURLException e) {
         
      } catch (IOException e) {
        
      }
    return connected;
    }


}


