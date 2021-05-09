# vaccine-slot-tracker
This tracker is simple java based application to find the availability of vaccine slots as per the pincode provided by the user   


Pre-requisite :
1. Any Java version (JRE) should be install in your windows machine.

Steps to run this application :
1. Download "vaccine-slot-tracker.zip" to your local windows machine and unzip the folder.

2. Update all the fields in configuration.json file. Currently only gmail email id can be configured. 
      {
      
        "email_sender" : "*****@gmail.com",
        
        "email_sender_password":"******",
        
        "age":30,
        
        "email_recipients" : ["*****@gmail.com", "****@gmail.com", "******@gmail.com"],
        
        "pincode":[412056, 412305]
     
     }
 
 3. Double click on batch file named "StartSlotFinder.bat"

