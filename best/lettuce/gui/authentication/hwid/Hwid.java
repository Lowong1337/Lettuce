package best.lettuce.gui.authentication.hwid;

import java.security.MessageDigest;

public class Hwid {
    public static String getHWID() {
        try {
            String toEncrypt = System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL") + System.getProperty("os.name") + System.getProperty("os.arch") + Runtime.getRuntime().availableProcessors() + System.getenv("PROCESSOR_ARCHITECTURE")  + System.getenv("NUMBER_OF_PROCESSORS");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(toEncrypt.getBytes());
            StringBuffer hexString = new StringBuffer();
            byte[] byteData = md.digest();
            byte[] var543645 = byteData;
            int var321324 = byteData.length;

            for(int var23983 = 0; var23983 < var321324; ++var23983) {
                byte aByteData = var543645[var23983];
                String hex = Integer.toHexString(255 & aByteData);
                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception var594859489) {
            var594859489.printStackTrace();
            return "Error";
        }
    }
}
