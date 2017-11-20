/**
* StateManager.java - A abstract class for implementing different states
* To do: Add implementation for singleton
*/

package com.example.bot.spring;
import com.rivescript.RiveScript;

//import java.io.IOException;

abstract class State {

    protected static SQLDatabaseEngine sql;

    static
    {
        sql = new SQLDatabaseEngine();
    };

    /**
     * Synchronize between SQL and Rivescript
     * @param userId String data type
     * @param bot RiveScript data type 
     */
    public void syncSQLWithRiveScript(String userId, RiveScript bot){
		sql.setUserInfo(userId, "state", bot.getUservar(userId, "state"));
		sql.setUserInfo(userId, "topic", bot.getUservar(userId, "topic"));
    }

    /**
     * Reply a message for input text in this state
     * Abstract class function
     * @param userId String data type
     * @param text String data type
     * @param bot RiveScript data type 
     * @return String data type as the reply
     */
    public abstract String reply(String userId, String text, RiveScript bot);
}