package models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.libs.F.ArchivedEventStream;
import play.libs.F.EventStream;
import play.libs.F.IndexedEvent;
import play.libs.F.Promise;

public class Lobby
{
	final ArchivedEventStream<Lobby.Event> lobbyEvents = 
			new ArchivedEventStream<Lobby.Event>(5);
	
	public EventStream<Lobby.Event> join(String player)
	{
		lobbyEvents.publish(new Join(player));
		return lobbyEvents.eventStream();
	}
	
	
	public void leave(String player)
	{
		lobbyEvents.publish(new Leave(player));
	}
	
	public void startGame(String gameId)
	{
		lobbyEvents.publish(new StartGame(gameId));
	}
	
	
	public Promise<List<IndexedEvent<Lobby.Event>>> nextMessages(long last)
	{
		return lobbyEvents.nextEvents(last);
	}
	
	
	public List<Lobby.Event> achive()
	{
		return lobbyEvents.archive();
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
	
	public static class Leave extends Event
	{
		final public String player;

		public Leave(String player)
		{
			super("leave");
			this.player = player;
		}	
	}
	
	//transition to GameWebSocket
	public static class StartGame extends Event
	{
		final public String gameId;
		public StartGame(String gameId)
		{
			super("startgame");
			this.gameId = gameId;
		}
	}
	
	
	//~~~~~~Factory
	static Map<String, Lobby> instances = new HashMap<String, Lobby>();
	
	public static Lobby get(String gameId)
	{
		if (!instances.containsKey(gameId))
			instances.put(gameId, new Lobby());
		return instances.get(gameId);
		
	
	}
}
