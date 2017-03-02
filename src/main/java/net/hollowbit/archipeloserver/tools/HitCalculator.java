package net.hollowbit.archipeloserver.tools;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.Direction;

public class HitCalculator {
	
	public static ArrayList<String> getCollRectsHit (Entity hitter, Entity hitted, float range, Direction directionOfHitter) {
		ArrayList<String> collRectsHit = new ArrayList<String>();
		for (CollisionRect rect : hitted.getCollisionRects()) {
			if (didEntityHitEntityCollRect(hitter, rect, range, directionOfHitter))
				collRectsHit.add(rect.name);
		}
		return collRectsHit;
	}
	
	private static boolean didEntityHitEntityCollRect (Entity hitter, CollisionRect hittedColRect, float range, Direction directionOfHitter) {
		Vector2 hitterCenterPoint = hitter.getCenterPoint();
		//Calculates if hitted entity is even within the scope of the hitter entity
		switch (directionOfHitter) {
		case UP:
			if (hittedColRect.y + hittedColRect.height < hitterCenterPoint.y)
				return false;
			break;
		case DOWN:
			if (hittedColRect.y > hitterCenterPoint.y)
				return false;
			break;
		case LEFT:
			if (hittedColRect.x > hitterCenterPoint.x)
				return false;
			break;
		case RIGHT:
			if (hittedColRect.x + hittedColRect.width < hitterCenterPoint.x)
				return false;
			break;
		case UP_RIGHT:
			if (hittedColRect.x + hittedColRect.width < hitterCenterPoint.x || hittedColRect.y + hittedColRect.height < hitterCenterPoint.y)
				return false;
			break;
		case UP_LEFT:
			if (hittedColRect.x > hitterCenterPoint.x || hittedColRect.y + hittedColRect.height < hitterCenterPoint.y)
				return false;
			break;
		case DOWN_RIGHT:
			if (hittedColRect.x + hittedColRect.width < hitterCenterPoint.x || hittedColRect.y > hitterCenterPoint.y)
				return false;
			break;
		case DOWN_LEFT:
			if (hittedColRect.x > hitterCenterPoint.x || hittedColRect.y > hitterCenterPoint.y)
				return false;
			break;
		}
		
		float circleDistanceX = Math.abs(hitterCenterPoint.x - hittedColRect.x);
	    float circleDistanceY = Math.abs(hitterCenterPoint.y - hittedColRect.y);

	    if (circleDistanceX > (hittedColRect.width / 2 + range)) { return false; }
	    if (circleDistanceY > (hittedColRect.height / 2 + range)) { return false; }

	    if (circleDistanceX <= (hittedColRect.width / 2)) { return true; } 
	    if (circleDistanceY <= (hittedColRect.height / 2)) { return true; }

	    double cornerDistanceSq = Math.pow((circleDistanceX - hittedColRect.width / 2), 2) + Math.pow((circleDistanceY - hittedColRect.height / 2), 2);

	    return (cornerDistanceSq <= Math.pow(range, 2));
	}
	
}
