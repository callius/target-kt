# Transfer from OSSRH Staging API to Central Publisher Portal.
BEARER=$(printf "$MAVEN_CENTRAL_TOKEN_ID:$MAVEN_CENTRAL_TOKEN" | base64)
curl --request POST \
   --include \
   --header "Authorization: Bearer $BEARER" \
   https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/io.target-kt
