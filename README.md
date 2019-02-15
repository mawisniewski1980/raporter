# Raporter JBehave
 This is very light class which can help you to create html file raport with data from jbehave .stats and .html files.

# How to run - command line
 java -jar Raporter.jar parametr
 parametr e.g.  ..\build\jbehave\
 java -jar Raporter.jar C:\project1\build\jbehave

# Result
 You find one 01_EmailReportEmbedded.html file in C:\project1\build\raport 

# Info
 Results are ordered and gruped by regex "[AFP]{1}\\d{2}(?!\\d)", e.g. 01_TestName_A13.. , 02_TestName_A26
 
 # For what?
  I used this raport file on Jenkins - Editable Email Notification to add this as emdded content in email.
