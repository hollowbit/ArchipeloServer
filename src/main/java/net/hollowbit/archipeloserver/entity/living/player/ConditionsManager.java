package net.hollowbit.archipeloserver.entity.living.player;

import java.util.ArrayList;
import java.util.HashSet;

import com.badlogic.gdx.utils.Json;

public class ConditionsManager {
	
	private HashSet<String> conditions;
	private Json json;
	
	public ConditionsManager (String conditionsJson) {
		json = new Json();
		
		ArrayList<String> conditionsArray = json.fromJson(ConditionsData.class, conditionsJson).conditions;
		
		for (String cond : conditionsArray)
			conditions.add(cond);
	}
	
	/**
	 * Returns the json array of conditions ready to save in the database
	 * @return
	 */
	public String getConditionsJson () {
		ConditionsData conditionsData = new ConditionsData();
		conditionsData.conditions = new ArrayList<String>();
		
		for (String cond : conditions)
			conditionsData.conditions.add(cond);
		
		return json.toJson(conditionsData);
	}
	
	/**
	 * Checks if the specified condition has been met by this player
	 * @param condition
	 * @return
	 */
	public boolean isConditionMet (String condition) {
		return conditions.contains(condition);
	}
	
	/**
	 * Set a condition to true
	 * @param condition
	 */
	public void setCondition (String condition) {
		
	}

	public class ConditionsData {
		
		public ArrayList<String> conditions;
		
	}
	
}
