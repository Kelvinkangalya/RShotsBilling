@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.11.9-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
echo DEBUG: Starting Rshots Billing in Diagnostic Mode...
echo DEBUG: JAVA_HOME is %JAVA_HOME%
cd composeApp
..\gradlew.bat :composeApp:run
pause
