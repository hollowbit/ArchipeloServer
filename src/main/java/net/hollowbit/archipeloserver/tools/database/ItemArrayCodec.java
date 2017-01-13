package net.hollowbit.archipeloserver.tools.database;

import java.util.ArrayList;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import net.hollowbit.archipeloserver.items.Item;

public class ItemArrayCodec implements Codec<Item[]> {
	
	protected Codec<Document> documentCodec;
	
	public ItemArrayCodec (Codec<Document> documentCodec) {
		this.documentCodec = documentCodec;
	}
	
	public void encode(BsonWriter writer, Item[] value, EncoderContext encoderContext) {
		Document document = new Document();
		
		ArrayList<Document> items = new ArrayList<Document>();
		for (int i = 0; i < value.length; i++) {
			if (value[i] == null)
				items.add(new Document());
			else
				items.add(new Document()
						.append("id", value[i].id)
						.append("color", value[i].color)
						.append("durability", value[i].durability)
						.append("style", value[i].style)
						.append("quantity", value[i].quantity)
						);
		}
		document.append("items", items);
		documentCodec.encode(writer, document, encoderContext);
	}

	public Class<Item[]> getEncoderClass() {
		return Item[].class;
	}

	public Item[] decode(BsonReader reader, DecoderContext decoderContext) {
		System.out.println("Test!");
		Document document = documentCodec.decode(reader, decoderContext);
		return decode(document);
	}
	
	public Item[] decode (Document document) {
		if (document == null || !document.containsKey("items"))
			return null;
		
		@SuppressWarnings("unchecked")
		ArrayList<Document> items = (ArrayList<Document>) document.get("items");
		
		Item[] value = new Item[items.size()];
		for (int i = 0; i < value.length; i++) {
			Document itemDoc = items.get(i);
			if (itemDoc.containsKey("id")) {
				Item item = new Item();
				item.id = itemDoc.getString("id");
				item.color = itemDoc.getInteger("color", item.color);
				item.durability = itemDoc.getInteger("durability", item.durability);
				item.style = itemDoc.getInteger("style", item.style);
				item.quantity = itemDoc.getInteger("quantity", item.quantity);
				value[i] = item;
			} else
				value[i] = null;
		}
		return value;
	}

}
