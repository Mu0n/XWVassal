cd %~dp0\mic\swxwmg.vmod-unpacked\images
del Thumbs.db
cd %~dp0\mic\
call gradlew downloadXwingData
call gradlew buildVmod %1

