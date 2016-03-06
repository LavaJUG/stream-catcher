#!/bin/bash

CATCHER_HOME=`dirname $0`

sudo modprobe v4l2loopback
java -jar $CATCHER_HOME/streamcaster.jar > /dev/null &
sleep 4
$CATCHER_HOME/firefox/firefox http://127.0.0.1:7777/ http://www.youtube.com > /dev/null &
