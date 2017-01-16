package net.hollowbit.archipeloserver.tools.database;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.sql.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.entity.living.player.PlayerData;

public class DatabaseManager {
	
	MongoClient client;
	MongoDatabase db;
	MongoCollection<Document> players;
	ItemArrayCodec itemArrayCodec;
	
	public DatabaseManager () {
		//Connect to database
		try {
			System.setProperty("DEBUG.MONGO", "false");
			System.setProperty("DB.TRACE", "false");
			Logger mongoLogger = Logger.getLogger( "com.mongodb" );
			mongoLogger.setLevel(Level.SEVERE);
			
			Codec<Document> defaultDocumentCodec = MongoClient.getDefaultCodecRegistry().get(Document.class);
			itemArrayCodec = new ItemArrayCodec(defaultDocumentCodec);
			
			CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
					MongoClient.getDefaultCodecRegistry(),
				    CodecRegistries.fromCodecs(itemArrayCodec)
				);
			
			MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecRegistry).build();
			
			client = new MongoClient("localhost:27017", options);
			client.getAddress();
			db = client.getDatabase("archipelo_server");
			players = db.getCollection("players");
			
			ArchipeloServer.getServer().getLogger().info("Connected to database!");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			ArchipeloServer.getServer().getLogger().error("Unable to connect to database!");
		}
	}
	
	/**
	 * Get player data of a specified player.
	 * @param name
	 * @param hbUuid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PlayerData getPlayerData (String name, String hbUuid) {
		PlayerData data = new PlayerData();
		
		Document doc = players.find(and(eq("name", name), eq("hbUuid", hbUuid))).first();
		data.name = name;
		data.bhUuid = hbUuid;
		data.id = doc.getString("uuid");
		data.x = doc.getDouble("x").floatValue();
		data.y = doc.getDouble("y").floatValue();
		data.island = doc.getString("island");
		data.map = doc.getString("map");
		data.uneditableEquippedInventory = itemArrayCodec.decode((Document) doc.get("uneditableEquippedInventory"));
		data.equippedInventory = itemArrayCodec.decode((Document) doc.get("equippedInventory"));
		data.cosmeticInventory = itemArrayCodec.decode((Document) doc.get("cosmeticInventory"));
		data.bankInventory = itemArrayCodec.decode((Document) doc.get("bankInventory"));
		data.inventory = itemArrayCodec.decode((Document) doc.get("inventory"));
		data.weaponInventory = itemArrayCodec.decode((Document) doc.get("weaponInventory"));
		data.consumablesInventory = itemArrayCodec.decode((Document) doc.get("consumablesInventory"));
		data.buffsInventory = itemArrayCodec.decode((Document) doc.get("buffsInventory"));
		data.ammoInventory = itemArrayCodec.decode((Document) doc.get("ammoInventory"));
		data.lastPlayed = doc.getDate("lastPlayedDate");
		data.creationDate = doc.getDate("creationDate");
		data.flags = (ArrayList<String>) doc.get("flags");
		
		return data;
	}
	
	/**
	 * Gets player data of all players belonging to the specified user
	 * @param hbUuid
	 * @return
	 */
	public ArrayList<PlayerData> getPlayerDataFromUser (String hbUuid) {
		ArrayList<PlayerData> datas = new ArrayList<PlayerData>();
		
		FindIterable<Document> iterable = players.find(eq("hbUuid", hbUuid));
		iterable.forEach(new Block<Document>() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void apply (Document doc) {
				PlayerData data = new PlayerData();

				data.bhUuid = hbUuid;
				data.id = doc.getString("uuid");
				data.name = doc.getString("name");
				data.x = doc.getDouble("x").floatValue();
				data.y = doc.getDouble("y").floatValue();
				data.island = doc.getString("island");
				data.map = doc.getString("map");
				data.uneditableEquippedInventory = itemArrayCodec.decode((Document) doc.get("uneditableEquippedInventory"));
				data.equippedInventory = itemArrayCodec.decode((Document) doc.get("equippedInventory"));
				data.cosmeticInventory = itemArrayCodec.decode((Document) doc.get("cosmeticInventory"));
				data.bankInventory = itemArrayCodec.decode((Document) doc.get("bankInventory"));
				data.inventory = itemArrayCodec.decode((Document) doc.get("inventory"));
				data.weaponInventory = itemArrayCodec.decode((Document) doc.get("weaponInventory"));
				data.consumablesInventory = itemArrayCodec.decode((Document) doc.get("consumablesInventory"));
				data.buffsInventory = itemArrayCodec.decode((Document) doc.get("buffsInventory"));
				data.ammoInventory = itemArrayCodec.decode((Document) doc.get("ammoInventory"));
				data.lastPlayed = doc.getDate("lastPlayedDate");
				data.creationDate = doc.getDate("creationDate");
				data.flags = (ArrayList<String>) doc.get("flags");
				
				datas.add(data);
			}
			
		});
		
		return datas;
	}
	
	/**
	 * Create a new table row with player data
	 * @param player
	 */
	public void createPlayer (Player player) {
		Thread thread = new Thread(new Runnable() {//Use a thread so that this task is done asynchronously
			@Override
			public void run() {
				players.insertOne(new Document()
						.append("hbUuid", player.getHollowBitUser().getUUID())
						.append("name", player.getName())
						.append("x", player.getLocation().getX())
						.append("y", player.getLocation().getY())
						.append("island", player.getLocation().getIsland().getName())
						.append("map", player.getLocation().getMap().getName())
						.append("uneditableEquippedInventory", player.getInventory().getUneditableEquippedInventory().getRawStorage())
						.append("equippedInventory", player.getInventory().getEquippedInventory().getRawStorage())
						.append("cosmeticInventory", player.getInventory().getCosmeticInventory().getRawStorage())
						.append("bankInventory", player.getInventory().getBankInventory().getRawStorage())
						.append("inventory", player.getInventory().getMainInventory().getRawStorage())
						.append("weaponInventory", player.getInventory().getWeaponInventory().getRawStorage())
						.append("consumablesInventory", player.getInventory().getConsumablesInventory().getRawStorage())
						.append("buffsInventory", player.getInventory().getBuffsInventory().getRawStorage())
						.append("ammoInventory", player.getInventory().getAmmoInventory().getRawStorage())
						.append("lastPlayedDate", player.getLastPlayedDate())
						.append("creationDate", player.getCreationDate())
						.append("flags", player.getFlagsManager().getFlagsList())
					);
			}
		});
		thread.start();
	}
	
	/**
	 * Returns whether a player with that name exists.
	 * @param name
	 * @return
	 */
	public boolean doesPlayerExist (String name) {
		return players.find(eq("name", name)).first() != null;
	}
	
	/**
	 * Update data of a player.
	 * @param player
	 */
	public void updatePlayer (Player player) {
		Thread thread = new Thread(new Runnable(){//Use a thread so that this task is done asynchronously
			@Override
			public void run() {
				players.updateOne(eq("uuid", player.getId()), new Document("$set", new Document()
						.append("name", player.getName())
						.append("x", player.getLocation().getX())
						.append("y", player.getLocation().getY())
						.append("island", player.getLocation().getIsland().getName())
						.append("map", player.getLocation().getMap().getName())
						.append("uneditableEquippedInventory", player.getInventory().getUneditableEquippedInventory().getRawStorage())
						.append("equippedInventory", player.getInventory().getEquippedInventory().getRawStorage())
						.append("cosmeticInventory", player.getInventory().getCosmeticInventory().getRawStorage())
						.append("bankInventory", player.getInventory().getBankInventory().getRawStorage())
						.append("inventory", player.getInventory().getMainInventory().getRawStorage())
						.append("weaponInventory", player.getInventory().getWeaponInventory().getRawStorage())
						.append("consumablesInventory", player.getInventory().getConsumablesInventory().getRawStorage())
						.append("buffsInventory", player.getInventory().getBuffsInventory().getRawStorage())
						.append("ammoInventory", player.getInventory().getAmmoInventory().getRawStorage())
						.append("lastPlayedDate", player.getLastPlayedDate())
						.append("flags", player.getFlagsManager().getFlagsList())
					));
			}
		});
		thread.start();
	}
	
	/**
	 * Deletes player belonging to HBU. If this HBU doesn't own this player, it won't be deleted
	 * @param name
	 * @param hbUuid
	 */
	public void deletePlayer (String name, String hbUuid) {
		Thread thread = new Thread(new Runnable(){//Use a thread so that this task is done asynchronously
			@Override
			public void run() {
				//Delete players in database
				players.deleteOne(and(eq("name", name), eq("hbUuid", hbUuid)));
			}
		});
		thread.start();
	}
	
	/**
	 * Gets player count for this user
	 * @param hbUuid UUID of user to test for
	 * @return
	 */
	public long getPlayerCount (String hbUuid) {
		return players.count();
	}
	
	public static final Date getCurrentDate () {
		return new Date(System.currentTimeMillis());
	}
	
	public void dispose () {
		client.close();
	}
	
}
