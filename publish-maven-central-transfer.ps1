# Transfer from OSSRH Staging API to Central Publisher Portal.
$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(("{0}:{1}" -f $Env:MAVEN_CENTRAL_TOKEN_ID, $Env:MAVEN_CENTRAL_TOKEN)))
Invoke-WebRequest `
    -Method POST `
    -Uri https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/io.target-kt `
    -Headers @{ Authorization = ("Bearer {0}" -f $base64AuthInfo) }
