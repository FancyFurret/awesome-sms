#!/bin/bash

PKG_NAME="com.eightbitforest.awesomesms"
DATABASE_NAME="AwesomeSMS.db"

adb shell "run-as $PKG_NAME chmod 666 /data/data/$PKG_NAME/databases/$DATABASE_NAME"
adb exec-out run-as $PKG_NAME cat databases/$DATABASE_NAME > $DATABASE_NAME
adb shell "run-as $PKG_NAME chmod 600 /data/data/$PKG_NAME/databases/$DATABASE_NAME"
