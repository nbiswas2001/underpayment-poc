package uk.gov.dwp.rbc.sp.underpayments.utils;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager;
import com.amazonaws.encryptionsdk.caching.CryptoMaterialsCache;
import com.amazonaws.encryptionsdk.caching.LocalCryptoMaterialsCache;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.rbc.sp.underpayments.config.AppConfig;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.amazonaws.encryptionsdk.CryptoAlgorithm.ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384;
import static com.amazonaws.regions.Regions.EU_WEST_2;

@Slf4j
@Component
public class CryptoUtils {

    @Autowired
    AppConfig config;

    private AwsCrypto awsCrypto;

    private CachingCryptoMaterialsManager dataKeyCache;

    private static final int DATA_KEY_CACHE_CAPACITY = 10;
    private static final int DATA_KEY_CACHE_MAX_PER_KEY = 10_000;
    private static final int DATA_KEY_CACHE_MAX_ENTRY_AGE_SECS = 600;

    final Map<String, String> encryptionContext = Collections.singletonMap("underpayments", "1.0");

    private static final DigestUtils digestUtils = new DigestUtils("SHA-256");

    //-----------------------------------------
    public String encrypt(String clearTxt) {
        val result = awsCrypto.encryptData(
                dataKeyCache,
                clearTxt.getBytes(StandardCharsets.UTF_8),
                encryptionContext
        );
        val encData = Base64.encodeBase64String(result.getResult());
        return encData;
    }

    //-----------------------------------------
    public String decrypt(String cipherTxt) {
        val encBytes = Base64.decodeBase64(cipherTxt);
        val result = awsCrypto.decryptData(
                dataKeyCache,
                encBytes);
        return new String(result.getResult(), StandardCharsets.UTF_8);
    }

    //-------------------------------------------
    public String toCitizenKey(String key){
        return config.isTestData()? key: digestUtils.digestAsHex(key);
    }

    //------------------------------
    @PostConstruct
    void init() {

        log.info("Initialising.");

        awsCrypto = AwsCrypto.builder()
                .withCommitmentPolicy(CommitmentPolicy.ForbidEncryptAllowDecrypt)
                .withEncryptionAlgorithm(ALG_AES_256_GCM_IV12_TAG16_HKDF_SHA384_ECDSA_P384)
                .build();

        val masterKeyProvider = KmsMasterKeyProvider.builder()
                .withDefaultRegion(EU_WEST_2.getName())
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .buildStrict(config.getKmsKeyArn());

        CryptoMaterialsCache cache = new LocalCryptoMaterialsCache(DATA_KEY_CACHE_CAPACITY);

        dataKeyCache =
                CachingCryptoMaterialsManager.newBuilder()
                        .withMasterKeyProvider(masterKeyProvider)
                        .withCache(cache)
                        .withMaxAge(DATA_KEY_CACHE_MAX_ENTRY_AGE_SECS, TimeUnit.SECONDS)
                        .withMessageUseLimit(DATA_KEY_CACHE_MAX_PER_KEY)
                        .build();
    }
}
