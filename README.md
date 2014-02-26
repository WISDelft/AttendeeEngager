Attendee Engager
=====================



This demo is designed to work with a full Java EE 6 server such as JBoss EAP 6 or AS 7. Although it should be possible to craft a deployment of this demo to a simpler web server, it's much simpler to deploy to an EE 6 capable app server.

Prerequisites
-------------

 * Maven 3 (run `mvn --version` on the command line to check)
 * An unzipped copy of JBoss AS 7 or EAP 6

 More detailed instructions can be found in our [Setup Tutorial](tutorial-guide/SETUP.adoc)

Build and deploy (production mode)
----------------

To build a .war file and deploy it to the local running JBoss EAP 6 or AS 7 instance:

    % mvn clean package jboss-as:deploy

Once the above command has completed, you should be able to access the app at the following URL:

    http://localhost:8080/errai-tutorial

More detailed instructions can be found [here](tutorial-guide/RUN.adoc)

Code and Refresh (development mode)
----------------

Using GWT DevMode, it is possible to instantly view changes to client code by simply refreshing the browser window. You should be able to start the demo in dev mode with this single command:

    % mvn clean gwt:run

When the GWT Dev Mode window opens, press "Launch Default Browser" to start the app.

To debug in development mode, set up two remote debuggers: one on port 8000 for the client-side code, and one on port 8001 for the server-side code. Then:
* Run `mvn clean gwt:debug`
* Start your server remote debugger
* Start your client remote debugger
* Press "Launch Default Browser"

See our development guide [here](tutorial-guide/DEVELOP.adoc) for more instructions on setting up dev mode and other details.

Build and deploy as native app to your mobile device
-----------------------------------------------------

The native application will have to know the absolute path to your server or cluster. For this demo you will have to change the REST and Errai Bus endpoint URL in Config.java and App.java (see comments) before building the native application.

To produce the native applications run:

    % mvn clean package -Pmobile,jboss7

After that you can simply install the native app on your phone or tablet. 

e.g for Android run:

    % adb install target/template/platforms/android/bin/HelloCordova-debug.apk

Troubleshooting
---------------

Here are some resources that may help if you encounter difficulties:
* [FAQ](tutorial-guide/FAQ.adoc)
* [Forum](https://community.jboss.org/en/errai)
* IRC : #errai @ freenode
