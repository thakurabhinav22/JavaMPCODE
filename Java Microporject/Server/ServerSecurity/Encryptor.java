package Server.ServerSecurity;

public class Encryptor {
    private static final String ENCRYPTION_KEY = "fncdngvdgvfbfrvvkudscjtcmrb";

    public String EncuserId(String ip) {
        StringBuilder encryptedIp = new StringBuilder();

       
        for (int i = 0; i < ip.length(); i++) {
            char ipChar = ip.charAt(i);
            char keyChar = ENCRYPTION_KEY.charAt(i % ENCRYPTION_KEY.length());
            
            // XOR operation
            int encryptedValue = (ipChar ^ keyChar);

            // encrypted value to a two-digit 
            encryptedIp.append(String.format("%02d", encryptedValue));
        }

        return encryptedIp.toString();
    }
}
