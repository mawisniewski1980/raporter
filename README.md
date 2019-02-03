# raporter

##Run
#java -jar Raporter.jar parameter-destination-jbehave-raport
#java -jar Raporter.jar D:\Pobieranie\reporter\jbehave

## Create jar file with default manifest
#jar cvf Raporter.jar *.class

## Create own manifest file
#manifest.mf
   Manifest-Version: 1.0
   Created-By: 1.8.0_91 (Oracle Corporation)
   Main-Class: Raporter

##Update jar Raporter.jar file
#jar cfm Raporter.jar manifest.mf *.class
