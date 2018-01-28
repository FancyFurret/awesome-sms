@echo off

SET ADB_LOCATION="C:\Users\fjone\AppData\local\Android\Sdk\platform-tools\adb.exe"
SET PKG_NAME="com.eightbitforest.awesomesms"
SET DATABASE_NAME="AwesomeSMS.db"

"%ADB_LOCATION%" shell "run-as %PKG_NAME% chmod 666 /data/data/%PKG_NAME%/databases/%DATABASE_NAME%"
"%ADB_LOCATION%" exec-out run-as %PKG_NAME% cat databases/%DATABASE_NAME% > %DATABASE_NAME%
"%ADB_LOCATION%" shell "run-as %PKG_NAME% chmod 600 /data/data/%PKG_NAME%/databases/%DATABASE_NAME%"
