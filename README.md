appengine-java-connection-pool
======================

Google App Engine frontend instances are not allowed to have threads that outlive request scope.
This restriction limits the connection pool library choice to DBCP 1.4. This is not the latest version
of DBCP, but it is the one that works with the above restriction. It does work with Java 7.

How to use
======================

Import into Eclipse, update the constants in the main servlet to your database.
You can now run the project as a Web Application. In your browser visit
http://localhost:8888/appengineconnectionpoolexample

Use the SQLManager as a singleton within your request. Get a connection for each
query, then give it back to the connection pool.

AppEngineConnectionPoolExampleServlet.java has the bulk of the example code.

License
======================
Apache v2. See the LICENSE file.
