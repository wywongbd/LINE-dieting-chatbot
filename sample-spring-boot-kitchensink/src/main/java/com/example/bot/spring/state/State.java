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
     * Reply a message for input text
     * @param text A String data type
     * @return A String data type
     */
    
    public void syncSQLWithRiveScript(String userId, RiveScript bot){
		sql.setUserInfo(userId, "state", bot.getUservar(userId, "state"));
		sql.setUserInfo(userId, "topic", bot.getUservar(userId, "topic"));
    }

    public abstract String reply(String userId, String text, RiveScript bot);
}