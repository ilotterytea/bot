package kz.ilotterytea.bot.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Hash utilities.
 * @author ilotterytea
 * @since 1.4
 */
public class HashUtils {
    public static String generateHmac256(String key, String data) {
        return generateHmac("sha256", data, key);
    }

    private static String generateHmac(
            String algorithm,
            String data,
            String key
    ) {
        try {
            SecretKeySpec spec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    algorithm
            );

            Mac mac = Mac.getInstance(algorithm);

            mac.init(spec);

            StringBuilder sb = new StringBuilder();
            for (byte b : mac.doFinal(data.getBytes(StandardCharsets.UTF_8))) {
                sb.append(String.format("%02X", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
