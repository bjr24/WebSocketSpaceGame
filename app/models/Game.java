package models;

import java.util.HashMap;
import java.util.Map;

import play.libs.F.ArchivedEventStream;
import play.libs.F.EventStream;


public class Game
{
	final ArchivedEventStream<Game.Event> gameEvents = 
			new ArchivedEventStream<Game.Event>(5);
	
	
	public EventStream<Game.Event> join(String player)
	{
		gameEvents.publish(new Join(player));
		return gameEvents.eventStream();
	}
	
	public void move(String player, String x, String y, 
			String rot, String thrust, String color)
	{
		gameEvents.publish(new Move(player, x, y, rot, thrust, color));
	}
	
	public void fire(String player, String x, String y, String rot)
	{
		gameEvents.publish(new Bullet(player, x, y, rot));
	}
	
	
	//~~~~~~Events
	public static abstract class Event
	{
		final public String type;
		public Event(String type)
		{
			this.type = type;
		}
	}
	
	public static class Join extends Event
	{
		final public String player;
		public Join(String player)
		{
			super("join");
			this.player = player;
		}
	}
	
	public static class Move extends Event
	{
		final public String player;
		final public String x;
		final public String y;
		final public String rot;
		final public String thrust;
		final public String color;
		public Move(String player, String x, String y, 
				String rot, String thrust, String color)
		{
			super("move");
			this.player = player;
			this.x = x;
			this.y = y;
			this.rot = rot;
			this.thrust = thrust;
			this.color = color;
		}
		public String toString()
		{
			return String.format( "move: %s&x: %s&y: %s&rot: %s&thrust: %s&color: %s"
								  ,player,  x,    y,    rot,    thrust,    color );
		}
	}
	
	public static class Bullet extends Event
	{
		final public String player;
		final public String x;
		final public String y;
		final public String rot;
		public Bullet(String player, String x, String y, String rot)
		{
			super("move");
			this.player = player;
			this.x = x;
			this.y = y;
			this.rot = rot;
		}
		public String toString()
		{
			return String.format( "bullet: %s&x: %s&y: %s&rot: %s"
								  ,player,    x,    y,    rot);
		}
	}
	
	
	//~~~~~~Factory
	static Map<String, Game> instances = new HashMap<String, Game>();
	
	public static Game get(String gameId)
	{
		if (!instances.containsKey(gameId))
			instances.put(gameId, new Game());
		return instances.get(gameId);
	}
}
