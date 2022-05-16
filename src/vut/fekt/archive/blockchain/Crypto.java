package vut.fekt.archive.blockchain;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Crypto implements Serializable {

    //vygeneruje RSA veřejný/soukromý klíč
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();

        return pair;
    }

    public static Blockchain loadBlockchain(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (Blockchain) ois.readObject();
    }

    public static void saveBlockchain(Blockchain blockchain, File file) throws IOException, ClassNotFoundException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(blockchain);
    }

    //podepíše byte[] soukormým RSA klíčem
    public static String sign(byte[] bytes, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(bytes);

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }

    //ověří platnost podpisu
    public static boolean verify(byte[] bytes, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(bytes);

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }

    //hashuje blok
    public static String blockHash(Block block){
        String s = block.getPreviousHash()+
                block.getSignature()+
                block.getPubKey()+
                block.getMetapath()+
                block.getBlockId()+
                block.getTimeStamp();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encodedhash = digest.digest(
                s.getBytes(UTF_8));
        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
        for (int i = 0; i < encodedhash.length; i++) {
            String hex = Integer.toHexString(0xff & encodedhash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getFileHash(String filepath) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        System.out.println("Hashing "+filepath);
        FileInputStream fis =new FileInputStream(filepath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try (DigestInputStream dis = new DigestInputStream(bis, md)) {
            while (dis.read() != -1) ; //empty loop to clear the data
            md = dis.getMessageDigest();
        }
        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }

    public static String getStringHash(String string) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        System.out.println("Hashing "+string);
        md.update(string.getBytes(UTF_8));
        byte[] bytes = md.digest();
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static String getHashOfFiles(String[] f) throws NoSuchAlgorithmException, IOException {
        String s = "";
        for (String file:f) {
            //s+=Crypto.getFileHash(folder.getAbsolutePath()+"/"+file);
            s+=Crypto.getFileHash(file);
        }
        return Crypto.getStringHash(s);
    }

    public static String getHashOfUrls(String[] f, String hostname, String folder) throws NoSuchAlgorithmException, IOException {
        String s = "";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        for (String file:f) {
            //file.replac
            URL path = new URL("http://"+hostname+"/archive/"+folder+"/"+file);
            InputStream in = path.openStream();
            BufferedInputStream bis = new BufferedInputStream(in);
            try (DigestInputStream dis = new DigestInputStream(bis, md)) {
                while (dis.read() != -1) ; //empty loop to clear the data
                md = dis.getMessageDigest();
            }
            // bytes to hex
            StringBuilder result = new StringBuilder();
            for (byte b : md.digest()) {
                result.append(String.format("%02x", b));
            }
            //System.out.println(result);
            s+=result;
            in.close();
            bis.close();
        }
        return Crypto.getStringHash(s);
    }

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String TRANSFORMATION = "AES";

    public static void encrypt(String key, File inputFile, File outputFile, String salt)
            throws CryptoException {
        cryptoFile(Cipher.ENCRYPT_MODE, key, inputFile,outputFile,salt);
    }

    public static void decrypt(String key, File inputFile, File outputFile,String salt)
            throws CryptoException {
        cryptoFile(Cipher.DECRYPT_MODE, key, inputFile, outputFile,salt);
    }


    public static void cryptoFile(int cipherMode, String password, File inputFile, File outputFile,String salt) throws CryptoException {
        try {
            SecretKey key =getKeyFromPassword(password, salt);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(cipherMode, key, generateIv());
            FileInputStream inputStream = new FileInputStream(inputFile);
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[64];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    outputStream.write(output);
                }
            }
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                outputStream.write(outputBytes);
            }
            inputStream.close();
            outputStream.close();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException
                |InvalidAlgorithmParameterException  | InvalidKeySpecException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        }
    }

    public static SecretKey getKeyFromPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), "AES");
        return secret;
    }

    public static IvParameterSpec generateIv() {
        String s1 = "1234567812345678";
        byte[] bytes = s1.getBytes();
        return new IvParameterSpec(bytes);
    }

    public static String serialize(Serializable o) throws IOException {            // serializace objektů do Stringu pro přenos
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static Object deserialize(String s)   {                                  // u přijatých dat se provede deserializace - dekódování
        Object o = null;
        try {
            byte[] data = Base64.getDecoder().decode(s);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o = ois.readObject();
            ois.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return o;
    }
}
//kryptograficke pomocne metody

