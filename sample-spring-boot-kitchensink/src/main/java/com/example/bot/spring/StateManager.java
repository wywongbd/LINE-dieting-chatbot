/**
* StateManager.java - A class for managing different states and transitions
*/

package com.example.bot.spring;

import com.example.bot.spring.DietbotController.DownloadedContent;

public class StateManager {
	// Constant values
	private final int STANDBY_STATE = 0;
	private final int INPUT_MENU_STATE = 3;
	private final int RECOMMEND_STATE = 4;
	// Must first go through InputMenuState before going to RecommendationState,
	// so 4 is not included
	private final int[] FROM_STANBY_STATE = {1, 2, 3, 5};

	// Value to keep track current state
    private int currentState = 0;
    private State[] states = {
    		new StanbyState(),
    		new CollectUserInfoState(),
    		new ProvideInfoState(),
    		new InputMenuState(),
    		new RecommendationState(),
    		new PostEatingState()
    	};

    /**
     * Default constructor for StateManager
     */
	public StateManager() {
	}

    /**
     * Get output message after inputting image
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
	public String chat(DownloadedContent jpg) {
		// Add exception handling if the input jpg is not recognized as menu
		return "Your image has been well received!";
	}

    /**
     * Get output message after inputting text
     * @param text A String data type
     * @return A String data type
     */
	public String chat(String text) {
		currentState = nextState(text);
		String replyText = states[currentState].reply(text);
		return replyText;
	}

    /**
     * Get the next state
     * @param text A String data type
     * @return A int data type
     */
	public int nextState(String text) {
		// Abit of hardcoding
		// To do: add all transitions in 2D arrays in constructor
		if(currentState != STANDBY_STATE) {
			// Current state is not stanby state
			if(states[STANDBY_STATE].checkTrigger(text)) {
				// Transition from any state to stanby state
				return STANDBY_STATE;
			}
			else if(currentState == INPUT_MENU_STATE && states[RECOMMEND_STATE].checkTrigger(text)) {
				// Transition from input menu to recommendation
				return RECOMMEND_STATE;
			}
		}
		else {    // Current state is stanby state
			for(int state: FROM_STANBY_STATE) {
				if(states[state].checkTrigger(text)) {
					// Transition from stanby state to other state
					return state;
				}
			}
		}
		return currentState;
	}
}