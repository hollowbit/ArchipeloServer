package net.hollowbit.archipeloserver.network.serialization;

import net.hollowbit.archipeloserver.network.Packet;

/**
 * Interface for Serializers of Packet objects.
 * @author vedi0boy
 *
 */
public interface Serializer {
	
	public static final String SEPARATOR = ";";
	
	public abstract byte[] serialize(Packet packet);

	public abstract Packet deserialize(byte[] data);
	
}
