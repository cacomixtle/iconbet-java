# ICON Bet in Java

You should already have a wallet created and a goloop node running locally

### build and deploy all score:

`./gradlew clean build optimizedJar deployToLocal -PkeystoreName=wallet.json -PkeystorePass=gochain`

### build and deploy a particular score

`./gradlew clean build daodice-score:optimizedJar daodice-score:deployToLocal -PkeystoreName=wallet.json -PkeystorePass=gochain`

