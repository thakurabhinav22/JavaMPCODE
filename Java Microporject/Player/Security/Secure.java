package Player.Security;

public class Secure {
    public String EncryptMessage(String msg, int key) {
        StringBuilder encryptedMessage = new StringBuilder();

        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            char encryptedChar = (char) (c ^ key); 
            encryptedMessage.append(encryptedChar);
        }
        return encryptedMessage.toString();
    }

    // Decrypts the message using XOR with the key
    public String DecryptMessage(String encryptedMsg, int key) {
        StringBuilder decryptedMessage = new StringBuilder();

        System.out.println("Decrypting with key: " + key);
    
        for (int i = 0; i < encryptedMsg.length(); i++) {
            char c = encryptedMsg.charAt(i);
            char decryptedChar = (char) (c ^ key); 
            decryptedMessage.append(decryptedChar);
        }
    
        String result = decryptedMessage.toString();

        result = result.replaceAll("[^\\p{Print}\\p{Space}]", "");
        
        return result;
    }

}
