web: java $JAVA_TOOL_OPTIONS -Dserver.port=$PORT -jar target/bot-1.0-jar-with-dependencies.jar
worker: sh target/bin/serverWorker
heroku ps:scale web=1