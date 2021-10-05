#!/bin/sh -x
export IFS=
MIN="768"
MAX="2000"
JAVA_HEAP_SIZE_MIN_PERF="2048"
JAVA_HEAP_SIZE_MAX_PERF="8192"
if [ "$HIGH_PERFORMANCE_MODE" = "true" ]; then
    MIN=$JAVA_HEAP_SIZE_MIN_PERF
    MAX=$JAVA_HEAP_SIZE_MAX_PERF
fi

APP_ROOT="/opt/app"
TRUST_ALIAS="loadbalancer"
KEYSTORE_ALIAS="tomcat"

EXISTING_CACERTS="/etc/ssl/certs/java/cacerts"
KEYSTORE_PASSWORD="password"
TRUSTSTORE_PASSWORD="changeit"


# Setup Trust store cert
if [ -z "$CACHAIN" ]; then
    echo "No CACHAIN passed in"
else
    echo $CACHAIN>truststore.crt
    keytool -import -alias $TRUST_ALIAS -file truststore.crt -keystore $EXISTING_CACERTS -storepass $TRUSTSTORE_PASSWORD -noprompt
    echo "Done importing trust store cert"

    rm truststore.crt
    keytool -list -keystore $EXISTING_CACERTS -storepass $TRUSTSTORE_PASSWORD -alias $TRUST_ALIAS
fi

# Setup HTTPS key/cert for application.yml to pick up
if [ -z "$TLS_KEY" ]; then
    echo "No TLS key supplied, generating a key..."
    keytool -genkeypair -alias tomcat -keyalg RSA -keysize 4096 -keystore keystore.jks -validity 365 -storepass $KEYSTORE_PASSWORD -noprompt -dname "CN=localhost,OU=,O=,L=,S=,C="
else
    echo "Using supplied keys"
    echo $TLS_KEY > chain.pem
    echo $TLS_CERT >> chain.pem

    cat chain.pem

    echo "Generating p12..."
    openssl pkcs12 -export -out chain.p12 -in chain.pem -password pass:$KEYSTORE_PASSWORD -name $KEYSTORE_ALIAS

    echo "Importing p12 into the keystore..."
    keytool -importkeystore -srckeystore chain.p12 -srcstoretype pkcs12 -srcstorepass $KEYSTORE_PASSWORD -destkeystore keystore.jks -deststoretype jks -deststorepass $KEYSTORE_PASSWORD -destkeypass $KEYSTORE_PASSWORD
fi

# Add other authority certs which is useful for local testing
if [ -z "$OTHER_CERT" ]; then
    echo "No other external certs supplied"
else
    echo "Other certs supplied, adding that to the trust store"
    echo $OTHER_CERT > other.crt
    keytool -import -alias othercert -file other.crt -keystore $EXISTING_CACERTS -storepass $TRUSTSTORE_PASSWORD -noprompt
    echo "Done importing other authority cert into trust store"
fi


/usr/bin/java -version
echo "/usr/bin/java -Xms${MIN}m -Xmx${MAX}m -jar app.jar"
/usr/bin/java -Xms${MIN}m -Xmx${MAX}m -jar app.jar
