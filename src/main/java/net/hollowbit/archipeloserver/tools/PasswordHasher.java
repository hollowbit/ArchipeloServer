package net.hollowbit.archipeloserver.tools;

import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import net.hollowbit.archipeloserver.ArchipeloServer;

public class PasswordHasher {
	
	private final Random RANDOM = new SecureRandom();
	private final int ITERATIONS = 100000;
	private final int KEY_LENGTH = 256;
	
	
	public byte[] getNextSalt() {
		byte[] salt = new byte[16];
	    RANDOM.nextBytes(salt);
	    return salt;
	}
	
	public byte[] hash(String passwordString, byte[] salt) {
	    PBEKeySpec spec = new PBEKeySpec(passwordString.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
	    try {
	    	SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	    	return skf.generateSecret(spec).getEncoded();
	    } catch (Exception e) {
	    	ArchipeloServer.getServer().getLogger().caution("Could not hash password: " + e.getMessage());
	    	return null;
	    } finally {
	    	spec.clearPassword();
	    }
	}
	
	public boolean isSamePassword(String passwordString, byte[] salt, byte[] expectedHash) {
	    byte[] pwdHash = hash(passwordString, salt);
	    if (pwdHash.length != expectedHash.length) return false;
	    for (int i = 0; i < pwdHash.length; i++) {
	    	if (pwdHash[i] != expectedHash[i]) return false;
	    }
	    return true;
	}
	
}
